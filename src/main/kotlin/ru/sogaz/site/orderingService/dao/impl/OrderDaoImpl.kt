package ru.sogaz.site.orderingService.dao.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.repository.OrderRepository
import ru.sogaz.site.orderingService.repository.SubOrderRepository
import java.math.BigDecimal
import java.util.UUID

@Service
class OrderDaoImpl(
    private val orderRepository: OrderRepository,
    private val subOrderRepository: SubOrderRepository,
) : OrderDao {
    @Transactional
    override fun upsertBatch(batch: List<OrderPayloadDto>) {
        val ordersById = LinkedHashMap<UUID, OrderEntity>()
        for (dto in batch) {
            val id = UUID.fromString(dto.orderId)
            ordersById
                .computeIfAbsent(id) {
                    OrderEntity(
                        id = id,
                        recipientEmail = dto.recipientEmail ?: "",
                        recipientPhone = dto.recipientPhone ?: "",
                    )
                }.apply {
                    recipientUserGdId = dto.recipientGdId
                    keyCard = dto.keyCard
                    saveCard = dto.saveCard
                    recurrent = dto.recurrent
                    paymentEndDate = dto.orderEndDate
                    recipientEmail = dto.recipientEmail ?: recipientEmail
                    recipientPhone = dto.recipientPhone ?: recipientPhone
                    recipientUserId = dto.recipientUserId
                    premiumAmount = (
                        (premiumAmount ?: BigDecimal.ZERO) +
                            dto.subOrders
                                .map { it.premiumAmount }
                                .fold(BigDecimal.ZERO, BigDecimal::add)
                                .takeIf { it > BigDecimal.ZERO }!!
                    )
                }
        }

        val orders = ordersById.values.toList()
        orderRepository.saveAll(orders)

        val orderById = orders.associateBy { it.id!! }

        val allSubs = ArrayList<SubOrderEntity>(batch.sumOf { it.subOrders.size })
        batch.forEach { dto ->
            val order = orderById[UUID.fromString(dto.orderId)]!!
            dto.subOrders.forEach { s ->
                allSubs +=
                    SubOrderEntity(
                        orderEntity = order,
                        policyId = s.policyId,
                        policyNumber = s.policyNumber,
                        contractId = s.contractId,
                        contractNumber = s.contractNumber,
                        insuranceProgram = s.insuranceProgram,
                        typeInsurance = s.typeInsurance,
                        premiumAmount = s.premiumAmount,
                        managerEmail = dto.managerEmail,
                    )
            }
        }

        subOrderRepository.saveAll(allSubs)
    }
}
