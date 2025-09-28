package ru.sogaz.site.orderingService.service.impl

import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import ru.sogaz.site.orderingService.dto.PaymentCreatedEvent
import ru.sogaz.site.orderingService.dto.PublishResult
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.PaymentEventProducer
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class PaymentEventProducerImpl(
    private val rabbit: RabbitTemplate,
    private val props: RabbitProps
) : PaymentEventProducer {
    override fun sendBatch(events: List<PaymentCreatedEvent>): PublishResult {
        val cds = mutableListOf<CorrelationData>()

        // 1) публикуем на ОДНОМ канале (быстро), каждому сообщению даём CorrelationData с id=orderId
        rabbit.invoke { ops ->
            events.forEach { ev ->
                val orderIdStr = ev.data.orderId.toString()
                val cd = CorrelationData(orderIdStr)     // это id попадёт в confirm.future
                ops.convertAndSend(
                    props.exchange,
                    ev.eventType,
                    ev,
                    { msg ->
                        msg.messageProperties.correlationId = orderIdStr
                        msg
                    },
                    cd
                )
                cds += cd
            }

        }

        // 2) собираем результаты по каждому сообщению
        val acked = mutableSetOf<UUID>()
        val nAcked = mutableMapOf<UUID, String?>()
        val timeouts = mutableSetOf<UUID>()

        cds.forEach { cd ->
            val orderId = UUID.fromString(cd.id)
            try {
                // Ждём подтверждение по конкретному сообщению
                val confirm = cd.future.get(10, TimeUnit.SECONDS)
                if (confirm.isAck) {
                    acked += orderId
                } else {
                    nAcked[orderId] = confirm.reason
                }
            } catch (e: TimeoutException) {
                timeouts += orderId
            }
        }
        return PublishResult(acked, nAcked, timeouts)
    }
}