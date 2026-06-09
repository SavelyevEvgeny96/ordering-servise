package ru.sogaz.site.orderingService.dao

import ru.sogaz.site.orderingService.dao.model.SubOrderSummary
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.util.UUID

interface SubOrderDao {
    fun upsertSubOrders(subs: List<SubOrderEntity>)

    fun findSubOrdersByOrderIds(orderIds: Collection<UUID>): List<SubOrderSummary>
}
