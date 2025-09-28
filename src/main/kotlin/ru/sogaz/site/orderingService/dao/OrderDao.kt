package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.PaymentCreatedEvent

interface OrderDao {
    fun upsertBatch(batch: List<OrderPayloadDto>): List<PaymentCreatedEvent>
}
