package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.PaymentCreatedEvent
import ru.sogaz.site.orderingService.dto.PublishResult

interface PaymentEventProducer {
    fun sendBatch(events: List<PaymentCreatedEvent>?): PublishResult
}