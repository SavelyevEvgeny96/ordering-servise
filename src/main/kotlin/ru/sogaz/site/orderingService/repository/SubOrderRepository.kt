package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.util.*

interface SubOrderRepository : JpaRepository<SubOrderEntity, UUID> {
    @Modifying
    @Transactional
    fun deleteByOrderEntityId(orderId: UUID): Long
}
