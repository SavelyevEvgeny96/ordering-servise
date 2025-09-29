package ru.sogaz.site.orderingService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.service.BuildBatchConsumerService
import ru.sogaz.site.orderingService.service.impl.BuildBatchConsumerServiceImpl
@Configuration
class ServiceConfig {

    @Bean
    fun buildBatchConsumerConfig(
        orderDao: OrderDao,
        subOrderDao: SubOrderDao,
        props: RabbitProps
    ): BuildBatchConsumerService =
        BuildBatchConsumerServiceImpl(
            orderDao = orderDao, subOrderDao = subOrderDao,
            props = props
        )
}