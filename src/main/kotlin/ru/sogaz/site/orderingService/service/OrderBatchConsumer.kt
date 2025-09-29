package ru.sogaz.site.orderingService.service

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.entity.OrderEntity

interface OrderBatchConsumer {
    fun handleBatch(messages: List<Message>, channel: Channel)
}
