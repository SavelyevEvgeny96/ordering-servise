package ru.sogaz.site.orderingService.service

import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.entity.OrderEntity

interface OrderBatchConsumer {
    fun handleBatch(payloads: List<OrderPayloadDto>)
}
