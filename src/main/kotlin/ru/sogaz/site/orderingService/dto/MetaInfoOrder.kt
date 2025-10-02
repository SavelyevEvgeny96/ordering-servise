package ru.sogaz.site.orderingService.dto

import java.time.Instant

data class MetaInfoOrder(
    val eventTimeIso: Instant?,
    val author: String,
    val routingKey: String,
)
