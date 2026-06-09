package ru.sogaz.site.orderingService.dao.model

import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import java.math.BigDecimal
import java.util.UUID

data class OrdersUserSearchFilter(
    val type: OrdersUserSearchType,
    val userId: String? = null,
    val gdId: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val contactCondition: OrdersUserContactCondition? = null,
)

enum class OrdersUserSearchType {
    USER_ID,
    GD_ID,
    EMAIL_OR_PHONE,
}

enum class OrdersUserContactCondition {
    OR,
    AND,
}

data class OrderSummary(
    val orderId: UUID,
    val premiumAmount: BigDecimal?,
    val status: OrderStatusesEnum,
)

data class SubOrderSummary(
    val orderId: UUID,
    val policyId: String?,
    val policyNumber: String?,
    val typeInsurance: String?,
    val insuranceProgram: String?,
    val premiumAmount: BigDecimal?,
)
