package ru.sogaz.site.orderingService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.orderingService.loggerFor

@ConfigurationProperties(prefix = "app.rabbit")
class RabbitProps {
    private val logger = loggerFor(javaClass)
    var exchange: String = "orders.exchange"
    var queueOrder: String = "orders.created.queue"
    var queuePayment: String = "payment.created.queue"
    var routingKeyOrder: String = "orders.created"
    var routingKeyPayment: String = "payments.created"


    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("exchange = $exchange")
        logger.info("queueOrder = $queueOrder")
        logger.info("queuePayment = $queuePayment")
        logger.info("routingKeyOrder = $routingKeyOrder")
        logger.info("routingKeyPayment = $routingKeyPayment")
    }
}
