package ru.sogaz.site.orderingService.dto

import java.util.*

data class PublishResult(
    val acked: Set<UUID>,
    val nAcked: Map<UUID, String?>,
    val timeouts: Set<UUID>
)