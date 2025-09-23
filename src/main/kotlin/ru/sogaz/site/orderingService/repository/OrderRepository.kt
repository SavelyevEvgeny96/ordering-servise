package ru.sogaz.site.orderingService.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.orderingService.entity.OrderEntity
import java.util.*

interface OrderRepository : JpaRepository<OrderEntity, UUID>
