package ru.sogaz.site.orderingService.service.impl

import org.springframework.http.HttpStatus
import ru.sogaz.site.orderingService.controller.ordersuser.OrdersUserData
import ru.sogaz.site.orderingService.controller.ordersuser.OrdersUserOrderDto
import ru.sogaz.site.orderingService.controller.ordersuser.OrdersUserRequest
import ru.sogaz.site.orderingService.controller.ordersuser.OrdersUserResponse
import ru.sogaz.site.orderingService.controller.ordersuser.OrdersUserSubOrderDto
import ru.sogaz.site.orderingService.controller.ordersuser.ValidationErrorDto
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dao.model.OrderSummary
import ru.sogaz.site.orderingService.dao.model.OrdersUserContactCondition
import ru.sogaz.site.orderingService.dao.model.OrdersUserSearchFilter
import ru.sogaz.site.orderingService.dao.model.OrdersUserSearchType
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.OrdersUserService
import ru.sogaz.site.orderingService.service.OrdersUserServiceResult
import java.math.BigDecimal

class OrdersUserServiceImpl(
    private val orderDao: OrderDao,
    private val subOrderDao: SubOrderDao,
) : OrdersUserService {
    companion object {
        private const val STATUS_SUCCESS = "success"
        private const val STATUS_ERROR = "error"
        private const val SUCCESS_CODE = 1101200
        private const val VALIDATION_ERROR_CODE = -1101640422
        private const val CONFLICT_ERROR_CODE = -1101409
        private const val INNER_ERROR_CONFLICT = "Conflict"
        private const val INNER_ERROR_VALIDATION = "UnprocessableEntity"
        private const val MESSAGE_VALIDATION = "Не все обязательные данные указаны корректно"
        private const val MESSAGE_CLIENT_NOT_FOUND =
            "Ошибка получения списка заказов клиента. Клиент с такими данными не найден"
        private const val MESSAGE_ORDERS_NOT_FOUND =
            "Ошибка получения списка заказов клиента. Заказы с указанными статусами не найдены"
        private val EMAIL_REGEX =
            Regex("^[A-Za-z0-9.!#\$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$")
        private val PHONE_REGEX = Regex("^\\+?[0-9 ]+$")
    }

    private val logger = loggerFor(javaClass)

    override fun getOrders(traceId: String, request: OrdersUserRequest): OrdersUserServiceResult {
        val validation = validateRequest(request)

        if (validation.errors.isNotEmpty()) {
            logger.info("Валидация запроса завершена с ошибками: {}", validation.errors)
            return OrdersUserServiceResult(
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
                body = OrdersUserResponse(
                    status = STATUS_ERROR,
                    code = VALIDATION_ERROR_CODE,
                    traceId = traceId,
                    innerError = INNER_ERROR_VALIDATION,
                    messageError = MESSAGE_VALIDATION,
                    errorsValidate = validation.errors,
                    data = null,
                ),
            )
        }

        val valid = validation.validated ?: return OrdersUserServiceResult(
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
            body = OrdersUserResponse(
                status = STATUS_ERROR,
                code = VALIDATION_ERROR_CODE,
                traceId = traceId,
                innerError = INNER_ERROR_VALIDATION,
                messageError = MESSAGE_VALIDATION,
                errorsValidate = validation.errors,
                data = null,
            ),
        )

        val orders = orderDao.findOrdersBySearch(valid.toSearchFilter())
        if (orders.isEmpty()) {
            logger.info("Клиент по параметрам {} не найден", valid.searchType)
            return buildConflictResponse(traceId, MESSAGE_CLIENT_NOT_FOUND)
        }

        val filteredOrders = filterByStatus(valid.status, orders)
        if (filteredOrders.isEmpty()) {
            logger.info("Для клиента {} не найдено заказов по статусу {}", valid.searchType, valid.status)
            return buildConflictResponse(traceId, MESSAGE_ORDERS_NOT_FOUND)
        }

        val orderIds = filteredOrders.map { it.orderId }
        val subOrders = subOrderDao.findSubOrdersByOrderIds(orderIds).groupBy { it.orderId }

        val ordersList = filteredOrders.map { order ->
            OrdersUserOrderDto(
                orderId = order.orderId.toString(),
                premiumAmount = order.premiumAmount.toAmountString(),
                subOrdersList = subOrders[order.orderId].orEmpty().map {
                    OrdersUserSubOrderDto(
                        policyId = it.policyId,
                        policyNumber = it.policyNumber,
                        typeInsurance = it.typeInsurance,
                        insuranceProgram = it.insuranceProgram,
                        premiumAmount = it.premiumAmount.toAmountString(),
                    )
                },
            )
        }

        return OrdersUserServiceResult(
            httpStatus = HttpStatus.OK,
            body = OrdersUserResponse(
                status = STATUS_SUCCESS,
                code = SUCCESS_CODE,
                traceId = traceId,
                innerError = null,
                messageError = null,
                errorsValidate = null,
                data = OrdersUserData(ordersList = ordersList),
            ),
        )
    }

    private fun buildConflictResponse(traceId: String, message: String) =
        OrdersUserServiceResult(
            httpStatus = HttpStatus.CONFLICT,
            body = OrdersUserResponse(
                status = STATUS_ERROR,
                code = CONFLICT_ERROR_CODE,
                traceId = traceId,
                innerError = INNER_ERROR_CONFLICT,
                messageError = message,
                errorsValidate = null,
                data = null,
            ),
        )

    private fun filterByStatus(status: OrdersUserStatus, orders: List<OrderSummary>): List<OrderSummary> =
        when (status) {
            OrdersUserStatus.UNPAID ->
                orders.filter { it.status == OrderStatusesEnum.NEW || it.status == OrderStatusesEnum.UPDATE }

            OrdersUserStatus.PAID ->
                orders.filter { it.status == OrderStatusesEnum.SUCCESS }

            OrdersUserStatus.ALL -> orders
        }

    private fun validateRequest(request: OrdersUserRequest): ValidationOutcome {
        val errors = mutableListOf<ValidationErrorDto>()

        val status = request.status.normalize()
        val statusEnum = status?.let { OrdersUserStatus.from(it) }
        if (status == null) {
            errors += ValidationErrorDto(param = "status", error = REQUIRED_VALUE)
        } else if (statusEnum == null) {
            errors += ValidationErrorDto(param = "status", error = "Возможные значения - unpaid, paid или all")
        }

        val searchName = request.searchName.normalize()
        val searchType = searchName?.let { OrdersUserSearchTypeExt.from(it) }
        if (searchName == null) {
            errors += ValidationErrorDto(param = "searchName", error = REQUIRED_VALUE)
        } else if (searchType == null) {
            errors += ValidationErrorDto(param = "searchName", error = "Указан недопустимый параметр поиска")
        }

        val normalizedUserId = request.userId.normalize()
        val normalizedGdId = request.gdId.normalize()
        val normalizedEmail = request.email.normalize()
        val normalizedPhone = request.phone.normalize()
        val normalizedCondition = request.condition.normalize()

        var contactCondition: OrdersUserContactCondition? = null

        when (searchType) {
            OrdersUserSearchType.USER_ID -> {
                if (normalizedUserId == null) {
                    errors += ValidationErrorDto(param = "userId", error = REQUIRED_VALUE)
                }
            }

            OrdersUserSearchType.GD_ID -> {
                if (normalizedGdId == null) {
                    errors += ValidationErrorDto(param = "gdId", error = REQUIRED_VALUE)
                }
            }

            OrdersUserSearchType.EMAIL_OR_PHONE -> {
                contactCondition = normalizedCondition?.let { OrdersUserContactConditionExt.from(it) }
                if (normalizedCondition == null) {
                    errors += ValidationErrorDto(param = "condition", error = REQUIRED_VALUE)
                } else if (contactCondition == null) {
                    errors += ValidationErrorDto(param = "condition", error = "Возможные значения - or и and")
                }

                when (contactCondition) {
                    OrdersUserContactCondition.AND -> {
                        if (normalizedEmail == null) {
                            errors += ValidationErrorDto(param = "email", error = REQUIRED_VALUE)
                        }
                        if (normalizedPhone == null) {
                            errors += ValidationErrorDto(param = "phone", error = REQUIRED_VALUE)
                        }
                    }

                    OrdersUserContactCondition.OR -> {
                        if (normalizedEmail == null && normalizedPhone == null) {
                            errors += ValidationErrorDto(param = "email", error = REQUIRED_VALUE)
                            errors += ValidationErrorDto(param = "phone", error = REQUIRED_VALUE)
                        }
                    }

                    null -> {}
                }

                normalizedEmail?.let {
                    if (!EMAIL_REGEX.matches(it)) {
                        errors += ValidationErrorDto(
                            param = "email",
                            error = "Значение должно содержать латинские буквы, цифры и специальные символы",
                        )
                    }
                }

                normalizedPhone?.let {
                    if (!PHONE_REGEX.matches(it)) {
                        errors += ValidationErrorDto(
                            param = "phone",
                            error = "Значение должно содержать только цифры, пробел и +",
                        )
                    }
                }
            }

            null -> {}
        }

        if (errors.isNotEmpty()) {
            return ValidationOutcome(errors = errors)
        }

        val validated = OrdersUserValidatedRequest(
            status = statusEnum!!,
            searchType = searchType!!,
            userId = normalizedUserId,
            gdId = normalizedGdId,
            email = normalizedEmail,
            phone = normalizedPhone,
            condition = contactCondition,
        )

        return ValidationOutcome(errors = emptyList(), validated = validated)
    }

    private data class ValidationOutcome(
        val errors: List<ValidationErrorDto>,
        val validated: OrdersUserValidatedRequest? = null,
    )

    private data class OrdersUserValidatedRequest(
        val status: OrdersUserStatus,
        val searchType: OrdersUserSearchType,
        val userId: String?,
        val gdId: String?,
        val email: String?,
        val phone: String?,
        val condition: OrdersUserContactCondition?,
    ) {
        fun toSearchFilter() =
            OrdersUserSearchFilter(
                type = searchType,
                userId = userId,
                gdId = gdId,
                email = email,
                phone = phone,
                contactCondition = condition,
            )
    }

    private enum class OrdersUserStatus(val value: String) {
        UNPAID("unpaid"),
        PAID("paid"),
        ALL("all");

        companion object {
            fun from(value: String) = entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
        }
    }

    private object OrdersUserSearchTypeExt {
        fun from(value: String) =
            when {
                value.equals("userId", ignoreCase = true) -> OrdersUserSearchType.USER_ID
                value.equals("gdId", ignoreCase = true) -> OrdersUserSearchType.GD_ID
                value.equals("emailOrPhone", ignoreCase = true) -> OrdersUserSearchType.EMAIL_OR_PHONE
                else -> null
            }
    }

    private object OrdersUserContactConditionExt {
        fun from(value: String) =
            when {
                value.equals("or", ignoreCase = true) -> OrdersUserContactCondition.OR
                value.equals("and", ignoreCase = true) -> OrdersUserContactCondition.AND
                else -> null
            }
    }

    private fun String?.normalize() = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun BigDecimal?.toAmountString(): String =
        this?.stripTrailingZeros()?.toPlainString() ?: ""

    private companion object {
        private const val REQUIRED_VALUE = "Не заполнено обязательное значение"
    }
}
