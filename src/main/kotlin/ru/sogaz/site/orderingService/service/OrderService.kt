package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.OrderPayloadDto

interface OrderService {
    fun processOrder(payload: OrderPayloadDto, eventTimeIso: String, author: String,routingKey: String,)
}