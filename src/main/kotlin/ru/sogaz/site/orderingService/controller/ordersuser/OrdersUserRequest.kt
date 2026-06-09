package ru.sogaz.site.orderingService.controller.ordersuser

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OrdersUserRequest(
    val status: String?,
    val searchName: String?,
    val userId: String?,
    val gdId: String?,
    val email: String?,
    val phone: String?,
    val condition: String?,
)
