package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.OrderMessageDto

interface OrderConsumer {
    fun handleOrder(order:OrderMessageDto)
}