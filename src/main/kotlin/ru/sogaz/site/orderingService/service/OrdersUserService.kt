package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.OrdersUserData
import ru.sogaz.site.orderingService.dto.OrdersUserRequest

interface OrdersUserService {
    fun findOrders(request: OrdersUserRequest): OrdersUserData
}
