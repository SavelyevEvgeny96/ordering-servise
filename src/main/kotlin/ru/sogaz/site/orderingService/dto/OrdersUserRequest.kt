package ru.sogaz.site.orderingService.dto

data class OrdersUserRequest(
    val state: String,
    val searchName: String,
    val uuid: String? = null,
    val gdId: String? = null,
    val userId: String? = null,
    val email: String? = null,
    val phone: String? = null,
)
