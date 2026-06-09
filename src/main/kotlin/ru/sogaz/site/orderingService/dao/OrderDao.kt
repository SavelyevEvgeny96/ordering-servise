package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.dao.model.OrderSummary
import ru.sogaz.site.orderingService.dao.model.OrdersUserSearchFilter
import ru.sogaz.site.orderingService.entity.OrderEntity

interface OrderDao {
    fun upsertOrders(orders: List<OrderEntity>)

    fun findOrdersBySearch(filter: OrdersUserSearchFilter): List<OrderSummary>
}
