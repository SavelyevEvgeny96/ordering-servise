package ru.sogaz.site.orderingService.dao.impl

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.TransactionRequiredException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.orderingService.dao.OrderDao
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.PaymentCreatedEvent
import ru.sogaz.site.orderingService.dto.PaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.loggerFor
import ru.sogaz.site.orderingService.properties.RabbitProps
import ru.sogaz.site.orderingService.repository.OrderRepository
import ru.sogaz.site.orderingService.repository.SubOrderRepository
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class OrderDaoImpl(
    private val jdbcTemplate: JdbcTemplate) : OrderDao {
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
    }

}