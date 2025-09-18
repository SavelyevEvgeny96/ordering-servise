package ru.sogaz.site.orderingService.service.impl

import ru.sogaz.site.orderingService.dto.OrderMessageDto
import ru.sogaz.site.orderingService.service.OrderConsumer
import ru.sogaz.site.orderingService.service.OrderService

class OrderConsumerImpl(private val orderService: OrderService):OrderConsumer {
    override fun handleOrder(order: OrderMessageDto) {
        orderService.processOrder(order)
    }
}