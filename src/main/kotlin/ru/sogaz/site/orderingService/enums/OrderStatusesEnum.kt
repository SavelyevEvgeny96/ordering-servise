package ru.sogaz.site.orderingService.enums

enum class OrderStatusesEnum {
    NEW,
    UPDATE,
    OVERDUE,
    MARKEDDEL,
    SUCCESS,
    ;

    fun isPaidFor() = this == SUCCESS

    fun isNotAvailable() = this in listOf(OVERDUE, MARKEDDEL)
}
