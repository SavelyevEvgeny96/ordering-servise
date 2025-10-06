package ru.sogaz.site.orderingService.controller.ordersuser

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.orderingService.service.OrdersUserService

@RestController
@RequestMapping("/v1/orders")
class OrdersUserController(
    private val ordersUserService: OrdersUserService,
) {
    @PostMapping("/ordersuser")
    fun getOrders(
        @RequestHeader("TraceId") traceId: String,
        @RequestHeader("Autorization") _: String,
        @RequestBody request: OrdersUserRequest,
    ): ResponseEntity<OrdersUserResponse> {
        val result = ordersUserService.getOrders(traceId, request)
        return ResponseEntity.status(result.httpStatus).body(result.body)
    }
}
