package ru.sogaz.site.orderingService.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.PaymentCreatedEvent
import ru.sogaz.site.orderingService.dto.PaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.BuildBatchConsumerService
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.HashSet


class BuildBatchConsumerServiceImpl(
    private val orderDao: OrderDao,
    private val subOrderDao: SubOrderDao,
    private val props: RabbitProps
) :
    BuildBatchConsumerService {
    companion object {
        const val BATCH_FAILED = "Сбой обработки пачки на orderId=%s, причина=%s"
        const val DUPLICATE = "Дубликат orderId в пачке, пропускаем: %s"
    }

    private val logger = loggerFor(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    override fun upsertBatch(batch: List<OrderPayloadDto>): List<PaymentCreatedEvent> {
        val nowIso = OffsetDateTime.now(ZoneOffset.UTC).toString()
        val currentOrderId = ""

        try {
            val (orders, subs) = prepareEntities(batch)

            if (orders.isNotEmpty()) orderDao.upsertOrders(orders)
            if (subs.isNotEmpty()) subOrderDao.upsertSubOrders(subs)

            return buildEvents(orders, nowIso)

        } catch (ex: Exception) {
            logger.error(BATCH_FAILED.format(currentOrderId, ex.message))
            throw ex // откат транзакции и отсутствие ACK
        }
    }

    private fun prepareEntities(batch: List<OrderPayloadDto>): Pair<List<OrderEntity>, List<SubOrderEntity>> {
        val seenOrderIds = HashSet<UUID>()
        val orders = mutableListOf<OrderEntity>()
        val subs = mutableListOf<SubOrderEntity>()

        for (dto in batch) {
            val id = UUID.fromString(dto.orderId)

            if (!seenOrderIds.add(id)) {
                logger.info(DUPLICATE.format(id))
                continue
            }

            val order = OrderEntity().apply {
                orderId = id
                recipientEmail = dto.recipientEmail ?: ""
                recipientPhone = dto.recipientPhone ?: ""
                recipientUserGdId = dto.recipientGdId
                keyCard = dto.keyCard
                saveCard = dto.saveCard
                recurrent = dto.recurrent
                paymentEndDate = dto.orderEndDate
                recipientUserId = dto.recipientUserId
                premiumAmount = dto.subOrders.map { it.premiumAmount }
                    .fold(BigDecimal.ZERO, BigDecimal::add)
                    .takeIf { it > BigDecimal.ZERO }
                createDate = Instant.now()
            }
            orders += order

            dto.subOrders.forEach { s ->
                subs += SubOrderEntity(
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
        return orders to subs
    }

    private fun buildEvents(orders: List<OrderEntity>, nowIso: String): List<PaymentCreatedEvent> {
        return orders.map { ord ->
            PaymentCreatedEvent(
                timestamp = nowIso,
                eventType = props.routingKeyPayment,
                data = PaymentData(
                    recurrent = ord.recurrent ?: false,
                    orderId = ord.orderId!!,
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
    }
}