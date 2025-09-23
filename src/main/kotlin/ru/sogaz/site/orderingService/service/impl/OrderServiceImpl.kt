package ru.sogaz.site.orderingService.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.service.OrderService

@Service
class OrderServiceImpl(
    private val orderDao: OrderDao
) : OrderService {

    private val logger = loggerFor(javaClass)

    @Transactional
    override fun processOrder(payload: OrderPayloadDto, eventTimeIso: String, author: String, routingKey: String) {
        try {
            logger.info("Processing order ${payload.orderId} with ${payload.subOrders.size} subOrders (rk=$routingKey)")
            orderDao.upsertOrderWithSubOrders(payload)
        } catch (ex: Exception) {
            logger.error("Order processing failed for")
            throw ex
        }
    }
}