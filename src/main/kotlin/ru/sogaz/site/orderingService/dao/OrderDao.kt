package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.PaymentCreatedEvent
import ru.sogaz.site.orderingService.entity.OrderEntity

interface OrderDao {
    fun upsertOrders(orders: List<OrderEntity>)
}
