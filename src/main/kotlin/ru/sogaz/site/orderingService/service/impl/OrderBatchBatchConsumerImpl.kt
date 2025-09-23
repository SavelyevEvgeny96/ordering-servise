package ru.sogaz.site.orderingService.service.impl

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.service.OrderBatchConsumer

@Service
class OrderBatchBatchConsumerImpl(
    private val orderDao: OrderDao,
) : OrderBatchConsumer {
    @RabbitListener(
        queues = ["\${app.rabbit.queue}"],
        containerFactory = "batchContainerFactory",
    )
    override fun handleBatch(
        payloads: List<OrderPayloadDto>,
        // Подумать как на пачку пробрасывать
//        @Header(name = "x-event-time") eventTimeIso: String,
//        @Header(name = "x-author") author: String,
//        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) routingKey: String,
    ) {
        orderDao.upsertBatch(payloads)
    }
}
