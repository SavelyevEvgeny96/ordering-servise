package ru.sogaz.site.orderingService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.orderingService.loggerFor

@ConfigurationProperties(prefix = "app.rabbit")
class RabbitProps {
    private val logger = loggerFor(javaClass)
    lateinit var exchange: String
    lateinit var queue: String
    lateinit var routingKey: String

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("exchange = $exchange")
        logger.info("queue = $queue")
        logger.info("routingKey = $routingKey")
    }
}