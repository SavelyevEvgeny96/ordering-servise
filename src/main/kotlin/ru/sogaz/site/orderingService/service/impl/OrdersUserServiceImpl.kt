package ru.sogaz.site.orderingService.service.impl

import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dto.OrdersUserData
import ru.sogaz.site.orderingService.dto.OrdersUserOrder
import ru.sogaz.site.orderingService.dto.OrdersUserRequest
import ru.sogaz.site.orderingService.dto.OrdersUserSubOrder
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.enums.OrdersUserSearchName
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.repository.OrderRepository
import ru.sogaz.site.orderingService.repository.SubOrderRepository
import ru.sogaz.site.orderingService.service.OrdersUserService
import java.util.Locale
import java.util.UUID

@Service
class OrdersUserServiceImpl(
    private val orderRepository: OrderRepository,
    private val subOrderRepository: SubOrderRepository,
) : OrdersUserService {
    private val logger = loggerFor(javaClass)

    override fun findOrders(request: OrdersUserRequest): OrdersUserData {
        val state = parseState(request.state)
        val search = parseSearchName(request.searchName)
        val searchValue = request.extractSearchValue(search)

        val specification = Specification<OrderEntity> { root, _, cb ->
            val predicates = mutableListOf(
                cb.equal(root.get<OrderStatusesEnum>("status"), state),
            )

            when (search) {
                OrdersUserSearchName.GUID ->
                    predicates += cb.equal(root.get<UUID>("orderId"), parseUuid(searchValue))
                OrdersUserSearchName.GD_ID ->
                    predicates += cb.equal(root.get<String>("recipientUserGdId"), searchValue)
                OrdersUserSearchName.USER_ID ->
                    predicates += cb.equal(root.get<String>("recipientUserId"), searchValue)
                OrdersUserSearchName.EMAIL ->
                    predicates += cb.equal(cb.lower(root.get("recipientEmail")), searchValue.lowercase(Locale.getDefault()))
                OrdersUserSearchName.PHONE ->
                    predicates += cb.equal(root.get<String>("recipientPhone"), searchValue)
            }

            cb.and(*predicates.toTypedArray())
        }

        val orders = orderRepository.findAll(specification)

        if (orders.isEmpty()) {
            logger.info(
                "Заказы не найдены: state={}, searchName={}, value={}",
                state,
                search,
                searchValue,
            )
            return OrdersUserData(state = state.name, total = 0, orders = emptyList())
        }

        val orderIds = orders.mapNotNull(OrderEntity::orderId)
        val subOrdersByOrderId = loadSubOrders(orderIds)

        val mappedOrders = orders.map { order ->
            val orderId = order.orderId ?: throw IllegalStateException("Не заполнено обязательное значение: orderId")
            OrdersUserOrder(
                orderId = orderId,
                status = order.status.name,
                paymentEndDate = order.paymentEndDate,
                premiumAmount = order.premiumAmount,
                recipientEmail = order.recipientEmail,
                recipientPhone = order.recipientPhone,
                recurrent = order.recurrent ?: false,
                keyCard = order.keyCard,
                saveCard = order.saveCard,
                subOrders = subOrdersByOrderId[orderId]?
                    .map(::mapSubOrder)
                    ?: emptyList(),
            )
        }

        return OrdersUserData(
            state = state.name,
            total = mappedOrders.size,
            orders = mappedOrders,
        )
    }

    private fun parseState(state: String?): OrderStatusesEnum {
        if (state.isNullOrBlank()) {
            throw IllegalArgumentException("Не заполнено обязательное значение: state")
        }

        return runCatching { OrderStatusesEnum.valueOf(state.trim().uppercase(Locale.getDefault())) }
            .getOrElse {
                throw IllegalArgumentException("Некорректное значение state: $state")
            }
    }

    private fun parseSearchName(searchName: String?): OrdersUserSearchName {
        if (searchName.isNullOrBlank()) {
            throw IllegalArgumentException("Не заполнено обязательное значение: searchName")
        }

        return OrdersUserSearchName.from(searchName)
    }

    private fun OrdersUserRequest.extractSearchValue(search: OrdersUserSearchName): String {
        val rawValue =
            when (search) {
                OrdersUserSearchName.GUID -> uuid
                OrdersUserSearchName.GD_ID -> gdId
                OrdersUserSearchName.USER_ID -> userId
                OrdersUserSearchName.EMAIL -> email
                OrdersUserSearchName.PHONE -> phone
            }

        val sanitized = rawValue?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Не заполнено обязательное значение: ${search.requestField}")

        return when (search) {
            OrdersUserSearchName.EMAIL -> sanitized.lowercase(Locale.getDefault())
            OrdersUserSearchName.PHONE -> sanitized.replace(" ", "")
            else -> sanitized
        }
    }

    private fun loadSubOrders(orderIds: List<UUID>): Map<UUID, List<SubOrderEntity>> {
        if (orderIds.isEmpty()) {
            return emptyMap()
        }

        return subOrderRepository
            .findByOrderEntityOrderIdIn(orderIds)
            .groupBy { it.orderEntity?.orderId }
            .filterKeys { it != null }
            .mapKeys { (key, _) -> key!! }
    }

    private fun parseUuid(value: String): UUID =
        runCatching { UUID.fromString(value) }
            .getOrElse {
                throw IllegalArgumentException("Некорректное значение uuid: $value")
            }

    private fun mapSubOrder(entity: SubOrderEntity) =
        OrdersUserSubOrder(
            policyId = entity.policyId,
            policyNumber = entity.policyNumber,
            contractId = entity.contractId,
            contractNumber = entity.contractNumber,
            insuranceProgram = entity.insuranceProgram,
            typeInsurance = entity.typeInsurance,
            premiumAmount = entity.premiumAmount,
            managerEmail = entity.managerEmail,
        )
}
