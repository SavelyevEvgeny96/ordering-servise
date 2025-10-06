package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import ru.sogaz.site.orderingService.entity.OrderEntity
import java.util.UUID

@Repository
interface OrderRepository : JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity>
