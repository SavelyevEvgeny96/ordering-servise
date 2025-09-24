package ru.sogaz.site.orderingService.dao.impl

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
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
    @PersistenceContext private val em: EntityManager   // для явного flush
) : OrderDao {

    @Transactional(rollbackFor = [Exception::class])
    override fun upsertBatch(batch: List<OrderPayloadDto>) {

        val newOrders = ArrayList<OrderEntity>(batch.size)

        val allSubs = ArrayList<SubOrderEntity>(batch.sumOf { it.subOrders.size })

        for (dto in batch) {
            val order = OrderEntity().apply {
                setIdFromExternal(UUID.fromString(dto.orderId))
                recipientEmail = dto.recipientEmail ?: ""
                recipientPhone = dto.recipientPhone ?: ""
                recipientUserGdId = dto.recipientGdId
                keyCard = dto.keyCard
                saveCard = dto.saveCard
                recurrent = dto.recurrent
                paymentEndDate = dto.orderEndDate
                recipientUserId = dto.recipientUserId
                premiumAmount = dto.subOrders
                    .map { it.premiumAmount }
                    .fold(BigDecimal.ZERO, BigDecimal::add)
                    .takeIf { it > BigDecimal.ZERO }
            }
            newOrders += order

            dto.subOrders.forEach { s ->
                allSubs += SubOrderEntity(
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
        }

        // 2) Вставляем РОДИТЕЛЕЙ без SELECT (persist)
        orderRepository.saveAll(newOrders)

        // 3) Явный flush — чтобы родительские строки уже были в БД,
        em.flush()

        subOrderRepository.saveAll(allSubs)
    }
}