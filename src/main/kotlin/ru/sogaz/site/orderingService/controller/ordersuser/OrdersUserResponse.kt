package ru.sogaz.site.orderingService.controller.ordersuser

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OrdersUserResponse(
    val status: String,
    val code: Int,
    val traceId: String,
    val innerError: String?,
    val messageError: String?,
    val errorsValidate: List<ValidationErrorDto>?,
    val data: OrdersUserData?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OrdersUserData(
    val ordersList: List<OrdersUserOrderDto>,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OrdersUserOrderDto(
    val orderId: String,
    val premiumAmount: String,
    val subOrdersList: List<OrdersUserSubOrderDto>,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OrdersUserSubOrderDto(
    @JsonProperty("policy_id")
    val policyId: String?,
    @JsonProperty("policy_number")
    val policyNumber: String?,
    @JsonProperty("type_insurance")
    val typeInsurance: String?,
    @JsonProperty("insurance_program")
    val insuranceProgram: String?,
    @JsonProperty("premium_amount")
    val premiumAmount: String,
)

data class ValidationErrorDto(
    val param: String,
    val error: String,
)
