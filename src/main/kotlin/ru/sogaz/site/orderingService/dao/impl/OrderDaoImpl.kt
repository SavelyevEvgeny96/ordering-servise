package ru.sogaz.site.orderingService.dao.impl

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.TransactionRequiredException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.PaymentCreatedEvent
import ru.sogaz.site.orderingService.dto.PaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.repository.OrderRepository
import ru.sogaz.site.orderingService.repository.SubOrderRepository
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class OrderDaoImpl(
    private val orderRepository: OrderRepository,
    private val subOrderRepository: SubOrderRepository,
    @PersistenceContext private val em: EntityManager,
    private val props: RabbitProps
) : OrderDao {
    companion object {
        const val BATCH_FAILED = "Сбой обработки пачки на orderId=%s, причина=%s"
        const val DUPLICATE = "Дубликат orderId в пачке, пропускаем: %s"
    }

    private val logger = loggerFor(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    override fun upsertBatch(batch: List<OrderPayloadDto>): List<PaymentCreatedEvent> {
        val nowIso = OffsetDateTime.now(ZoneOffset.UTC).toString()
        var currentOrderId = ""
        try {
            val seenOrderIds = HashSet<UUID>()
            val ordersById = LinkedHashMap<UUID, OrderEntity>()
            val newOrders = ArrayList<OrderEntity>(batch.size)
            val allSubs = ArrayList<SubOrderEntity>(batch.sumOf { it.subOrders.size })

            for (dto in batch) {
                currentOrderId = dto.orderId
                val id = UUID.fromString(dto.orderId)

                // если дубль — пропускаем целиком
                if (!seenOrderIds.add(id)) {
                    logger.info(DUPLICATE.format(id))
                    continue
                }

                // создаём единый экземпляр OrderEntity для этого id
                val order = OrderEntity().apply {
                    setIdFromExternal(id)
                    recipientEmail = dto.recipientEmail ?: ""
                    recipientPhone = dto.recipientPhone ?: ""
                    recipientUserGdId = dto.recipientGdId
                    keyCard = dto.keyCard
                    saveCard = dto.saveCard
                    recurrent = dto.recurrent
                    paymentEndDate = dto.orderEndDate
                    recipientUserId = dto.recipientUserId
                    val sum = dto.subOrders.map { it.premiumAmount }
                        .fold(BigDecimal.ZERO, BigDecimal::add)
                    premiumAmount = sum.takeIf { it > BigDecimal.ZERO }
                }

                newOrders += order
                ordersById[id] = order

                dto.subOrders.forEach { s ->
                    allSubs += SubOrderEntity(
                        orderEntity = order,
                        policyId = s.policyId,
                        policyNumber = s.policyNumber,
                        contractId = s.contractId,
                        contractNumber = s.contractNumber,
                        insuranceProgram = s.insuranceProgram,
                        typeInsurance = s.typeInsurance,
                        premiumAmount = s.premiumAmount,
                        managerEmail = dto.managerEmail
                    )
                }
            }

            if (newOrders.isNotEmpty()) orderRepository.saveAll(newOrders)
            em.flush()
            if (allSubs.isNotEmpty()) subOrderRepository.saveAll(allSubs)

            // события — одно на заказ (без полисных полей), только по уникальным orderId
            return ordersById.values.map { ord ->
                PaymentCreatedEvent(
                    timestamp = nowIso,
                    eventType = props.routingKeyPayment,
                    data = PaymentData(
                        recurrent = ord.recurrent ?: false,
                        orderId = ord.id!!,
                        premiumAmount = ord.premiumAmount,
                        saveCard = ord.saveCard,
                        keyCard = ord.keyCard,
                        recipientEmail = ord.recipientEmail,
                        recipientPhone = ord.recipientPhone,
                        dateCreate = ord.updateDate?.atOffset(ZoneOffset.UTC)?.toString(),
                        dateEnd = ord.paymentEndDate?.atOffset(ZoneOffset.UTC)?.toString()
                    )
                )
            }
        } catch (ex: Exception) {
            logger.error(BATCH_FAILED.format(currentOrderId, ex.message))
            throw ex // откат транзакции и отсутствие ACK
        }
    }

}