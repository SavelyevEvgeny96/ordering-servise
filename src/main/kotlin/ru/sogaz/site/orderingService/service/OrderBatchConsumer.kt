package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.OrderPayloadDto

interface OrderBatchConsumer {
    fun handleBatch(payloads: List<OrderPayloadDto>)
}