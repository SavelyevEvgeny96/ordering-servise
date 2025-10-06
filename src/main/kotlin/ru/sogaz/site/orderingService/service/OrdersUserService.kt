package ru.sogaz.site.orderingService.service

import org.springframework.http.HttpStatus
import ru.sogaz.site.orderingService.controller.ordersuser.OrdersUserRequest
import ru.sogaz.site.orderingService.controller.ordersuser.OrdersUserResponse

data class OrdersUserServiceResult(
    val httpStatus: HttpStatus,
    val body: OrdersUserResponse,
)

interface OrdersUserService {
    fun getOrders(traceId: String, request: OrdersUserRequest): OrdersUserServiceResult
}
