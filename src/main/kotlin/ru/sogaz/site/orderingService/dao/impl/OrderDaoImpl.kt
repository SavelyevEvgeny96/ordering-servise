package ru.sogaz.site.orderingService.dao.impl

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.loggerFor
import java.sql.Timestamp

@Service
class OrderDaoImpl(
    private val jdbcTemplate: JdbcTemplate
) : OrderDao {

    private val logger = loggerFor(javaClass)

    override fun upsertOrders(orders: List<OrderEntity>) {
        val sql = """
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

        logger.info("👉 Старт batch upsertOrders: size=${orders.size}")

        jdbcTemplate.dataSource!!.connection.use { conn ->
            conn.autoCommit = false
            conn.prepareStatement(sql).use { ps ->
                for (o in orders) {
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
                    ps.addBatch()
                }
                logger.info("👉 Выполняем executeBatch() для ${orders.size} записей")
                ps.executeBatch()
            }
            logger.info("✅ Выполнен executeBatch(), коммитим транзакцию")
            conn.commit()
        }

        logger.info("✅ Завершён upsertOrders: size=${orders.size}")
    }
}