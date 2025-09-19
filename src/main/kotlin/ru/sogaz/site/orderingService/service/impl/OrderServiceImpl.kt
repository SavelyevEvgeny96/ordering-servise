package ru.sogaz.site.orderingService.service.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dto.OrderMessageDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.OrderService
@Service
class OrderServiceImpl() : OrderService {
    private val logger = loggerFor(javaClass)
    override fun processOrder(order: OrderMessageDto) {
        try {
            // TODO: через DAO сохранить заказ в БД пока так до того как появится докер для развертывания
            logger.info("Processing order ${order.orderId} with ${order.payments.size} payments")

        } catch (ex: Exception) {
            logger.error("Order processing failed")

        }
    }
}