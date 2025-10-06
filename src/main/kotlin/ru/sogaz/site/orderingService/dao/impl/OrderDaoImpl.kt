package ru.sogaz.site.orderingService.dao.impl

import org.springframework.jdbc.core.JdbcTemplate
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dao.model.OrderSummary
import ru.sogaz.site.orderingService.dao.model.OrdersUserContactCondition
import ru.sogaz.site.orderingService.dao.model.OrdersUserSearchFilter
import ru.sogaz.site.orderingService.dao.model.OrdersUserSearchType
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.loggerFor
import java.sql.Timestamp
import java.util.UUID

class OrderDaoImpl(
    private val jdbcTemplate: JdbcTemplate,
) : OrderDao {
    companion object {
        private const val LOG_START = "Старт batch upsertOrders: size=%d"
        private const val LOG_EXECUTE = "Выполняем batchUpdate() для %d записей"
        private const val LOG_DONE = "Завершён upsertOrders: size=%d"
    }

    private val logger = loggerFor(javaClass)

    override fun upsertOrders(orders: List<OrderEntity>) {
        val sql =
            """
            INSERT INTO orders (
                order_id, create_date, recipient_email, recipient_phone,
                premium_amount, payment_end_date, key_card, save_card,
                recurrent, recipient_user_gd_id, recipient_user_id, update_date
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
            ON CONFLICT (order_id) DO UPDATE
              SET recipient_email      = EXCLUDED.recipient_email,
                  recipient_phone      = EXCLUDED.recipient_phone,
                  premium_amount       = EXCLUDED.premium_amount,
                  payment_end_date     = EXCLUDED.payment_end_date,
                  key_card             = EXCLUDED.key_card,
                  save_card            = EXCLUDED.save_card,
                  recurrent            = EXCLUDED.recurrent,
                  recipient_user_gd_id = EXCLUDED.recipient_user_gd_id,
                  recipient_user_id    = EXCLUDED.recipient_user_id,
                  update_date          = NOW()
            """.trimIndent()

        logger.info(LOG_START.format(orders.size))

        jdbcTemplate.batchUpdate(sql, orders, orders.size) { ps, o ->
            ps.setObject(1, o.orderId)
            ps.setTimestamp(2, o.createDate?.let { Timestamp.from(it) })
            ps.setString(3, o.recipientEmail)
            ps.setString(4, o.recipientPhone)
            ps.setBigDecimal(5, o.premiumAmount)
            ps.setTimestamp(6, o.paymentEndDate?.let { Timestamp.from(it) })
            ps.setString(7, o.keyCard)
            ps.setObject(8, o.saveCard)
            ps.setObject(9, o.recurrent)
            ps.setString(10, o.recipientUserGdId)
            ps.setString(11, o.recipientUserId)
        }

        logger.info(LOG_EXECUTE.format(orders.size))
        logger.info(LOG_DONE.format(orders.size))
    }

    override fun findOrdersBySearch(filter: OrdersUserSearchFilter): List<OrderSummary> {
        val (whereClause, params) = buildWhereClause(filter)

        val sql =
            """
            SELECT o.order_id, o.premium_amount, o.status
            FROM orders o
            WHERE $whereClause
            ORDER BY o.create_date DESC
            """.trimIndent()

        return jdbcTemplate.query(sql, params.toTypedArray()) { rs, _ ->
            OrderSummary(
                orderId = rs.getObject("order_id", UUID::class.java),
                premiumAmount = rs.getBigDecimal("premium_amount"),
                status = rs.getString("status")
                    ?.let { OrderStatusesEnum.valueOf(it) }
                    ?: OrderStatusesEnum.NEW,
            )
        }
    }

    private fun buildWhereClause(filter: OrdersUserSearchFilter): Pair<String, List<Any?>> {
        return when (filter.type) {
            OrdersUserSearchType.USER_ID ->
                "o.recipient_user_id = ?" to listOf(filter.userId)

            OrdersUserSearchType.GD_ID ->
                "o.recipient_user_gd_id = ?" to listOf(filter.gdId)

            OrdersUserSearchType.EMAIL_OR_PHONE -> buildEmailPhoneClause(filter)
        }
    }

    private fun buildEmailPhoneClause(filter: OrdersUserSearchFilter): Pair<String, List<Any?>> {
        return when (filter.contactCondition) {
            OrdersUserContactCondition.AND ->
                "o.recipient_email = ? AND o.recipient_phone = ?" to listOf(filter.email, filter.phone)

            OrdersUserContactCondition.OR -> {
                val conditions = mutableListOf<String>()
                val params = mutableListOf<Any?>()

                filter.email?.let {
                    conditions += "o.recipient_email = ?"
                    params += it
                }

                filter.phone?.let {
                    conditions += "o.recipient_phone = ?"
                    params += it
                }

                if (conditions.isEmpty()) {
                    "(1 = 0)" to emptyList()
                } else {
                    "(${conditions.joinToString(" OR ")})" to params
                }
            }

            null ->
                "(1 = 0)" to emptyList()
        }
    }
}
