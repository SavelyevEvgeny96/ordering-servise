package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.OrderPayloadDto

interface OrderConsumer {
    fun handleOrder(payload: OrderPayloadDto, eventTimeIso: String, author: String,routingKey: String,)
}