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

    override fun upsertOrderWithSubOrders(dto: OrderPayloadDto) {
        val orderId = UUID.fromString(dto.orderId)

        // 1) upsert заказа
        val order = orderRepository.findById(orderId).orElse(
            OrderEntity(
                id = orderId,
                recipientEmail = dto.recipientEmail ?: "",
                recipientPhone = dto.recipientPhone ?: "",
            )
        ).apply {
            recipientUserGdId = dto.recipientGdId
            keyCard = dto.keyCard
            saveCard = dto.saveCard
            recurrent = dto.recurrent
            paymentEndDate = dto.orderEndDate
            recipientEmail = dto.recipientEmail ?: recipientEmail
            recipientPhone = dto.recipientPhone ?: recipientPhone
            recipientUserId = dto.recipientUserId
            premiumAmount = dto.subOrders
                .map { it.premiumAmount }
                .fold(BigDecimal.ZERO, BigDecimal::add)
                .takeIf { it > BigDecimal.ZERO }
        }
        orderRepository.save(order) // insert или update


        subOrderRepository.deleteByOrderEntityId(orderId)

        val subs = dto.subOrders.map { s ->
            SubOrderEntity(
                orderEntity = order,
                policyId = s.policyId,
                policyNumber = s.policyNumber,
                contractId = s.contractId,
                contractNumber = s.contractNumber,
                insuranceProgram = s.insuranceProgram,
                typeInsurance = s.typeInsurance,
                premiumAmount = s.premiumAmount,
                managerEmail = dto.managerEmail
            )
        }
        subOrderRepository.saveAll(subs)
    }
}