package ru.sogaz.site.orderingService.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.orderingService.dto.OrdersUserError
import ru.sogaz.site.orderingService.dto.OrdersUserRequest
import ru.sogaz.site.orderingService.dto.OrdersUserResponse
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.OrdersUserService

@RestController
@RequestMapping("/v1/orders")
class OrdersUserController(
    private val ordersUserService: OrdersUserService,
) {
    private val logger = loggerFor(javaClass)

    @PostMapping("/ordersuser")
    fun getOrders(
        @RequestHeader(value = TRACE_ID_HEADER, required = false) traceId: String?,
        @RequestBody request: OrdersUserRequest,
    ): ResponseEntity<OrdersUserResponse> =
        try {
            val data = ordersUserService.findOrders(request)
            ResponseEntity.ok(
                OrdersUserResponse(
                    data = data,
                    code = HttpStatus.OK.value(),
                    status = SUCCESS_STATUS,
                    traceId = traceId,
                    message = null,
                    errors = emptyList(),
                ),
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("Ошибка валидации при получении заказов клиента: {}", ex.message)
            val message = ex.message ?: DEFAULT_VALIDATION_ERROR
            val errorField = resolveField(message)
            ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(
                    OrdersUserResponse(
                        data = null,
                        code = HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        status = ERROR_STATUS,
                        traceId = traceId,
                        message = message,
                        errors = listOf(OrdersUserError(field = errorField, message = message)),
                    ),
                )
        } catch (ex: Exception) {
            logger.error("Внутренняя ошибка при получении заказов клиента", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    OrdersUserResponse(
                        data = null,
                        code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        status = ERROR_STATUS,
                        traceId = traceId,
                        message = INTERNAL_ERROR_MESSAGE,
                        errors = emptyList(),
                    ),
                )
        }

    private fun resolveField(message: String): String =
        when {
            message.contains("state", ignoreCase = true) -> "state"
            message.contains("searchName", ignoreCase = true) -> "searchName"
            message.contains("uuid", ignoreCase = true) -> "uuid"
            message.contains("gdId", ignoreCase = true) || message.contains("gd_id", ignoreCase = true) -> "gdId"
            message.contains("userId", ignoreCase = true) || message.contains("user_id", ignoreCase = true) -> "userId"
            message.contains("email", ignoreCase = true) -> "email"
            message.contains("phone", ignoreCase = true) -> "phone"
            else -> "request"
        }

    companion object {
        private const val TRACE_ID_HEADER = "TraceId"
        private const val SUCCESS_STATUS = "SUCCESS"
        private const val ERROR_STATUS = "ERROR"
        private const val DEFAULT_VALIDATION_ERROR = "Не заполнено обязательное значение"
        private const val INTERNAL_ERROR_MESSAGE = "Внутренняя ошибка сервера"
    }
}
