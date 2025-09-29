package ru.sogaz.site.orderingService.entity

import jakarta.persistence.Transient
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.domain.Persistable
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
@Entity
@Table(name = "orders")
data class OrderEntity(

    @Id
    @Column(name = "order_id", columnDefinition = "uuid")
    var orderId: UUID? = null,

    @Column(name = "recipient_user_gd_id")
    var recipientUserGdId: String? = null,

    @Column(name = "key_card")
    var keyCard: String? = null,

    @Column(name = "save_card")
    var saveCard: Boolean? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: OrderStatusesEnum = OrderStatusesEnum.NEW,

    @Column(name = "recurrent")
    var recurrent: Boolean? = null,

    @Column(name = "payment_end_date")
    var paymentEndDate: Instant? = null,

    @Column(name = "premium_amount", precision = 19, scale = 2)
    var premiumAmount: BigDecimal? = null,

    @Column(name = "recipient_email", nullable = false)
    var recipientEmail: String = "",

    @Column(name = "recipient_phone", nullable = false)
    var recipientPhone: String = "",

    @Column(name = "recipient_user_id")
    var recipientUserId: String? = null,

    @Column(name = "create_date", updatable = false)
    var createDate: Instant? = null,

    @Column(name = "update_date")
    var updateDate: Instant? = null
)
