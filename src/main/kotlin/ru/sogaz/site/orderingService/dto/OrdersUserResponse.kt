package ru.sogaz.site.orderingService.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrdersUserResponse(
    val data: OrdersUserData?,
    val code: Int,
    val status: String,
    val traceId: String?,
    val message: String?,
    val errors: List<OrdersUserError>,
)

data class OrdersUserData(
    val state: String,
    val total: Int,
    val orders: List<OrdersUserOrder>,
)

data class OrdersUserOrder(
    val orderId: UUID,
    val status: String,
    val paymentEndDate: Instant?,
    val premiumAmount: BigDecimal?,
    val recipientEmail: String,
    val recipientPhone: String,
    val recurrent: Boolean,
    val keyCard: String?,
    val saveCard: Boolean?,
    val subOrders: List<OrdersUserSubOrder>,
)

data class OrdersUserSubOrder(
    val policyId: String,
    val policyNumber: String,
    val contractId: String?,
    val contractNumber: String?,
    val insuranceProgram: String?,
    val typeInsurance: String?,
    val premiumAmount: BigDecimal?,
    val managerEmail: String?,
)

data class OrdersUserError(
    val field: String,
    val message: String,
)
