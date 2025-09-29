package ru.sogaz.site.orderingService.service.impl

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.BuildBatchConsumerService
import ru.sogaz.site.orderingService.service.OrderBatchConsumer
import ru.sogaz.site.orderingService.service.PaymentEventProducer

@Service
class OrderBatchConsumerImpl(
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val paymentProducer: PaymentEventProducer,
) : OrderBatchConsumer {

    companion object {
        private const val SUCCESSFUL_QUEUE_PROCESSING =
            "Обработка записи из очереди успешно произведена: routingKey=%s, author=%s, eventTime=%s"
        private const val BATCH_SUMMARY =
            "Итог обработки пачки: количество=%d, длительность(мс)=%d"
    }

    private val logger = loggerFor(javaClass)

    @RabbitListener(
        queues = ["\${app.rabbit.queue-order}"],
        containerFactory = "batchContainerFactory"
    )
    override fun handleBatch(payloads: List<OrderPayloadDto>) {
        val started = System.nanoTime()

        // 1) запись в БД + сбор событий (транзакция внутри BuildBatchConsumerServiceImpl)
        val events = buildBatchConsumerService.upsertBatch(payloads)

        // 2) публикация и логирование — уже после успешного коммита
        paymentProducer.sendBatch(events)

        val metaByOrderId = payloads.associateBy(
            keySelector = { it.orderId },
            valueTransform = { it.metaInfo.lastOrNull() }
        )

        events.forEach { ev ->
            val meta = metaByOrderId[ev.data.orderId.toString()]
            logger.info(
                SUCCESSFUL_QUEUE_PROCESSING.format(
                    meta?.routingKey,
                    meta?.author,
                    meta?.eventTimeIso
                )
            )
        }

        val tookMs = (System.nanoTime() - started) / 1_000_000
        logger.info(BATCH_SUMMARY.format(payloads.size, tookMs))
    }
}