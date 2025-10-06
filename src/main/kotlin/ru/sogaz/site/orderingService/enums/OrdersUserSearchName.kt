package ru.sogaz.site.orderingService.enums

enum class OrdersUserSearchName(
    val requestField: String,
) {
    GUID("uuid"),
    GD_ID("gdId"),
    USER_ID("userId"),
    EMAIL("email"),
    PHONE("phone"),
    ;

    companion object {
        fun from(value: String): OrdersUserSearchName =
            when (value.trim().lowercase()) {
                "guid" -> GUID
                "gd_id" -> GD_ID
                "user_id" -> USER_ID
                "email" -> EMAIL
                "phone" -> PHONE
                else -> throw IllegalArgumentException("Некорректное значение searchName: $value")
            }
    }
}
