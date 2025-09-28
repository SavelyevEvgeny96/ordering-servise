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
    }

    private val log = loggerFor(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    override fun upsertBatch(batch: List<OrderPayloadDto>): List<PaymentCreatedEvent> {
        val nowIso = OffsetDateTime.now(ZoneOffset.UTC).toString()
        var currentOrderId = ""
        try {
            val newOrders = ArrayList<OrderEntity>(batch.size)
            val allSubs = ArrayList<SubOrderEntity>(batch.sumOf { it.subOrders.size })
            val ordersMap = HashMap<UUID, OrderEntity>(batch.size)

            for (dto in batch) {
                currentOrderId = dto.orderId
                val order = OrderEntity().apply {
                    setIdFromExternal(UUID.fromString(dto.orderId))
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
                ordersMap[order.id!!] = order

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

            return ordersMap.values.map { ord ->
                PaymentCreatedEvent(
                    timestamp = nowIso,
                    eventType = props.routingKeyPayment, // если это field для eventType
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
            log.error(BATCH_FAILED.format(currentOrderId, ex.message))
            throw ex // ОБЯЗАТЕЛЬНО пробрасываем, чтобы был rollback и не было ACK
        }
    }

}