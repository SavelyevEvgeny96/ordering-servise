package ru.sogaz.site.orderingService.service.impl

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.BuildBatchConsumerService
import ru.sogaz.site.orderingService.service.OrderBatchConsumer
import ru.sogaz.site.orderingService.service.PaymentEventProducer


 class OrderBatchConsumerImpl(
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val paymentProducer: PaymentEventProducer,
    private val messageConverter: MessageConverter
) : OrderBatchConsumer {

    companion object {
        private const val SUCCESSFUL_QUEUE_PROCESSING =
            "Обработка записи из очереди успешно произведена: routingKey=%s, author=%s, eventTime=%s"
        private const val BATCH_SUMMARY =
            "Итог обработки пачки: количество=%d, длительность(мс)=%d"
        private const val ACK_SUCCESSFUL = "ACK отправлен для deliveryTag=%d\", size=%d"
    }

    private val logger = loggerFor(javaClass)

    @RabbitListener(
        queues = ["\${app.rabbit.queue-order}"],
        containerFactory = "batchContainerFactory"
    )
    override fun handleBatch(messages: List<Message>, channel: Channel) {
        val deliveryTag = messages.last().messageProperties.deliveryTag

        // конвертим в DTO
        val payloads = messages.map {
            messageConverter.fromMessage(it) as OrderPayloadDto
        }
        val started = System.nanoTime()

        try {
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

            //  подтверждаем пачку
            channel.basicAck(deliveryTag, true)
            logger.debug(ACK_SUCCESSFUL.format(deliveryTag, payloads.size))
        } catch (ex: Exception) {
            logger.error("Ошибка при обработке пачки: ${ex.message}")

            //  возвращаем пачку в очередь
            channel.basicNack(deliveryTag, true, true)
            logger.debug("N_ACK отправлен для deliveryTag=$deliveryTag, size=${payloads.size}")

            throw ex
        }
    }
}