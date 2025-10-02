package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.PaymentCreatedEvent

interface BuildBatchConsumerService {
    fun upsertBatch(batch: List<OrderPayloadDto>): List<PaymentCreatedEvent>
}
