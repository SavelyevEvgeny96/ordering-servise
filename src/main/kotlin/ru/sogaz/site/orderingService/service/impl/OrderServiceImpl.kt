package ru.sogaz.site.orderingService.service.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.OrderService

@Service
class OrderServiceImpl() : OrderService {
    private val logger = loggerFor(javaClass)
    override fun processOrder(payload: OrderPayloadDto, eventTimeIso: String, author: String,routingKey: String) {
        try {


            logger.info("Processing order ${payload.orderId} with ${payload.subOrders.size} subOrders")

        } catch (ex: Exception) {
            logger.error("Order processing failed")

        }
    }
}