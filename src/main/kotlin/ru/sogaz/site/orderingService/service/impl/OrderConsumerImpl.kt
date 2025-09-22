package ru.sogaz.site.orderingService.service.impl

import jakarta.validation.Valid
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.service.OrderConsumer
import ru.sogaz.site.orderingService.service.OrderService

@Service
class OrderConsumerImpl(private val orderService: OrderService) : OrderConsumer {
    @RabbitListener(queues = ["\${app.rabbit.queue}"])

    override fun handleOrder(
        @Valid @Payload payload: OrderPayloadDto,
        @Header(name = "x-event-time") eventTimeIso: String,
        @Header(name = "x-author") author: String,
        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) routingKey: String,

        ) {
        orderService.processOrder(payload, eventTimeIso, author,routingKey)
    }
}