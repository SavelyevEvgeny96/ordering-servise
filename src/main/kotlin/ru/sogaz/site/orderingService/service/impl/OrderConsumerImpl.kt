package ru.sogaz.site.orderingService.service.impl

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dto.OrderMessageDto
import ru.sogaz.site.orderingService.service.OrderConsumer
import ru.sogaz.site.orderingService.service.OrderService

@Service
class OrderConsumerImpl(private val orderService: OrderService) : OrderConsumer {
    @RabbitListener(queues = ["\${app.rabbit.queue}"])
    override fun handleOrder(order: OrderMessageDto) {
        orderService.processOrder(order)
    }
}