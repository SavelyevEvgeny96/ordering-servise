package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.OrderMessageDto

interface OrderService {
    fun processOrder(order: OrderMessageDto)
}