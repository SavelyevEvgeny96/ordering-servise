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
        payloads: List<OrderPayloadDto>
    ) {
        orderDao.upsertBatch(payloads)
    }
}
