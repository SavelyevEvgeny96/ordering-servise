package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.dto.OrderPayloadDto

interface OrderDao {
    fun upsertBatch(batch: List<OrderPayloadDto>)
}