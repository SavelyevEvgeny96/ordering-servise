package ru.sogaz.site.orderingService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.SubOrderDao
import ru.sogaz.site.orderingService.dao.impl.OrderDaoImpl
import ru.sogaz.site.orderingService.dao.impl.SubOrderDaoImpl

@Configuration
class DaoConfig {
    @Bean
    fun orderDaoConfig(jdbcTemplate: JdbcTemplate): OrderDao = OrderDaoImpl(jdbcTemplate = jdbcTemplate)

    @Bean
    fun subOrderDao(jdbcTemplate: JdbcTemplate): SubOrderDao = SubOrderDaoImpl(jdbcTemplate = jdbcTemplate)
}