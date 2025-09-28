package ru.sogaz.site.orderingService.service.impl

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.PaymentEventProducer

@Service
class OrderBatchConsumerImpl(
    private val orderDao: OrderDao,
    private val paymentProducer: PaymentEventProducer,
) {

    companion object {
        const val SUCCESSFUL_QUEUE_PROCESSING =
            "Обработка записи из очереди успешно произведена: routingKey=%s, author=%s, eventTime=%s"
        const val BATCH_SUMMARY = "Итог обработки пачки: количество=%d, длительность(мс)=%d"

    }

    private val logger = loggerFor(javaClass)

    @RabbitListener(queues = ["\${app.rabbit.queue-order}"], containerFactory = "batchContainerFactory")
    fun handleBatch(payloads: List<OrderPayloadDto>) {
        val started = System.nanoTime()

        // Для логов после коммит подготовим мету по orderId
        val metaByOrderId = payloads.associateBy(
            keySelector = { it.orderId },
            valueTransform = { it.metaInfo.lastOrNull() }
        )

        // 1) запись в БД + сбор событий (в транзакции внутри DAO)
        val events = orderDao.upsertBatch(payloads)

        // 2) публикуем и логируем только после коммита DAO-транзакции
        org.springframework.transaction.support.TransactionSynchronizationManager
            .registerSynchronization(object : org.springframework.transaction.support.TransactionSynchronization {
                override fun afterCommit() {
                    // отправка пачкой (в продюсере просто цикл convertAndSend)
                    paymentProducer.sendBatch(events)

                    // логи "успешно обработано" на каждый orderId
                    events.forEach { ev ->
                        val meta = metaByOrderId[ev.data.orderId.toString()]
                        logger.info(
                            SUCCESSFUL_QUEUE_PROCESSING.format(
                                meta?.routingKey ,
                                meta?.author,
                                meta?.eventTimeIso
                            )
                        )
                    }

                    val tookMs = (System.nanoTime() - started) / 1_000_000
                    logger.info(BATCH_SUMMARY.format(payloads.size, tookMs))
                }
            })
    }
}