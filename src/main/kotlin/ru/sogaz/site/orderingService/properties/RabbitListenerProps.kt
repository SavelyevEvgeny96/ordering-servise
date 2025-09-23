package ru.sogaz.site.orderingService.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.rabbit.listener")
data class RabbitListenerProps(
    var batchSize: Int = 100,
    var prefetch: Int = 200,
    var concurrency: Int = 3,
    var maxConcurrency: Int = 8,
    var receiveTimeoutMs: Long = 500,
)
