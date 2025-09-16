package ru.sogaz.site.orderingService

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.sogaz.site.orderingService.properties.AppInfoProperties

@SpringBootApplication
@ConfigurationPropertiesScan("ru.sogaz.site.api-gateway.properties")
@EnableConfigurationProperties(AppInfoProperties::class)

class OrderingServiceApplication

fun main(args: Array<String>) {
    runApplication<OrderingServiceApplication>(*args)
}

fun <T> loggerFor(clazz: Class<T>) =
    ru.sogaz.core.logger.LoggerFactory
        .getApiLogger(clazz)
