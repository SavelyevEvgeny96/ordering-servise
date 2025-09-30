package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.entity.OrderEntity

interface OrderDao {
    fun upsertOrders(orders: List<OrderEntity>)
}
