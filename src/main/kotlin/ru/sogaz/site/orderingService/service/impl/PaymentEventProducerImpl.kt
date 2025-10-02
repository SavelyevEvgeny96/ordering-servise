package ru.sogaz.site.orderingService.service.impl

import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import ru.sogaz.site.orderingService.dto.PaymentCreatedEvent
import ru.sogaz.site.orderingService.dto.PublishResult
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.PaymentEventProducer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PaymentEventProducerImpl(
    private val rabbit: RabbitTemplate,
    private val props: RabbitProps,
) : PaymentEventProducer {
    companion object {
        private const val CONFIRMED_LOG = " Сообщение подтверждено брокером: orderId=%s"
        private const val N_ACK_LOG = " Сообщение отклонено брокером: orderId=%s, причина=%s"
        private const val RETURNED_LOG = " Сообщение возвращено брокером: %s, reply=%s"
        private const val PUBLISHED_LOG = " Отправлено событие: orderId=%s, routingKey=%s"
        private const val NO_CONFIRM_LOG = " Нет подтверждения на данный момент: orderId=%s"
    }

    private val logger = loggerFor(javaClass)

    private val confirmed = ConcurrentHashMap<UUID, Boolean>()
    private val errors = ConcurrentHashMap<UUID, String?>()

    init {
        rabbit.setConfirmCallback { correlation, ack, cause ->
            val id = correlation?.id ?: return@setConfirmCallback
            val orderId = UUID.fromString(id)

            if (ack) {
                confirmed[orderId] = true
                logger.debug(CONFIRMED_LOG.format(orderId))
            } else {
                errors[orderId] = cause
                logger.error(N_ACK_LOG.format(orderId, cause))
            }
        }

        rabbit.setReturnsCallback { returned ->
            logger.error(RETURNED_LOG.format(returned.message, returned.replyText))
        }
    }

    override fun sendBatch(events: List<PaymentCreatedEvent>): PublishResult {
        if (events.isEmpty()) {
            return PublishResult(emptySet(), emptyMap(), emptySet())
        }

        val acked = mutableSetOf<UUID>()
        val nAcked = mutableMapOf<UUID, String?>()

        events.forEach { ev ->
            val orderId = ev.data.orderId
            val cd = CorrelationData(orderId.toString())

            rabbit.convertAndSend(
                props.exchange,
                ev.eventType,
                ev,
                { msg ->
                    msg.messageProperties.correlationId = orderId.toString()
                    msg
                },
                cd,
            )
            logger.debug(PUBLISHED_LOG.format(orderId, ev.eventType))
        }

        events.forEach { ev ->
            val orderId = ev.data.orderId
            if (confirmed.remove(orderId) == true) {
                acked += orderId
            } else if (errors.containsKey(orderId)) {
                nAcked[orderId] = errors.remove(orderId)
            } else {
                logger.warn(NO_CONFIRM_LOG.format(orderId))
            }
        }

        return PublishResult(acked, nAcked, emptySet())
    }
}
