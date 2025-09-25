package ru.sogaz.site.orderingService.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant

data class MetaInfoOrder(
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    val eventTimeIso: Instant?,
    val author: String,
    val routingKey: String
)