package ru.sogaz.site.orderingService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.PaymentCreatedEvent
import ru.sogaz.site.orderingService.dto.PaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.mapper.OrderMapper
import ru.sogaz.site.orderingService.mappers.PaymentEventMapper
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.BuildBatchConsumerService
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.collections.HashSet

open class BuildBatchConsumerServiceImpl(
    private val orderDao: OrderDao,
    private val subOrderDao: SubOrderDao,
    private val props: RabbitProps,
    private val orderMapper: OrderMapper,
    private val paymentEventMapper: PaymentEventMapper
) : BuildBatchConsumerService {
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

            return paymentEventMapper.toPaymentEvents(orders, nowIso, props.routingKeyPayment)
        } catch (ex: Exception) {
            logger.error(BATCH_FAILED.format(currentOrderId, ex.message))
            throw ex // откат транзакции и отсутствие ACK
        }
    }

    private fun prepareEntities(batch: List<OrderPayloadDto>): Pair<List<OrderEntity>, List<SubOrderEntity>> {
        val seenOrderIds = mutableSetOf<UUID>()
        val orders = mutableListOf<OrderEntity>()
        val subs = mutableListOf<SubOrderEntity>()

        batch.forEach { dto ->
            val id = UUID.fromString(dto.orderId)

            if (!seenOrderIds.add(id)) {
                logger.info(DUPLICATE.format(id))
                return@forEach
            }

            val order = orderMapper.toOrderEntity(dto)
            orders += order

            val subOrders = orderMapper.toSubOrderEntities(dto.subOrders, order, dto.managerEmail)
            subs += subOrders
        }

        return orders to subs
    }

    private fun buildEvents(
        orders: List<OrderEntity>,
        nowIso: String,
    ): List<PaymentCreatedEvent> =
        orders.map { ord ->
            PaymentCreatedEvent(
                timestamp = nowIso,
                eventType = props.routingKeyPayment,
                data =
                    PaymentData(
                        recurrent = ord.recurrent ?: false,
                        orderId = ord.orderId!!,
                        premiumAmount = ord.premiumAmount,
                        saveCard = ord.saveCard,
                        keyCard = ord.keyCard,
                        recipientEmail = ord.recipientEmail,
                        recipientPhone = ord.recipientPhone,
                        dateCreate = ord.updateDate?.atOffset(ZoneOffset.UTC)?.toString(),
                        dateEnd = ord.paymentEndDate?.atOffset(ZoneOffset.UTC)?.toString(),
                    ),
            )
        }
}
