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
            // TODO: что бы  сохранить заказ через DAO  в БД нужно понять раз вопросов
            // TODO: 1. задать вопросы вале сейчас в описании orderId летит к нам из вне и падает в очередь корректно ли это ?
            // TODO: 2.Всегда ли будет стандартным формат контракта сообщения что приходит в очередь
            // TODO: 3.что транслировать в логи на сколько подробно задать сереже и вале
            // TODO: 4. вопрос о количестве записей в очереди что бы понимать как настроить слушателя и
            //  распределить нагрузку для хорошей работы БД так как там будут задействованы сразу две таблицы
            logger.info("Processing order ${order.orderId} with ${order.payments.size} payments")

        } catch (ex: Exception) {
            logger.error("Order processing failed")

        }
    }
}