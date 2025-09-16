package ru.sogaz.site.orderingService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener::class)
data class Order(
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "order_id")
    val id: UUID? = null,
    @Column(name = "recipient_user_gd_id")
    val recipientUserGdId: String? = null,
    @Column(name = "key_card")
    val keyCard: String? = null,
    @Column(name = "save_card")
    val saveCard: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    val status: OrderStatus? = null,
    @Column(name = "recurrent")
    val recurrent: String? = null,
    @Column(name = "payment_end_date")
    val paymentEndDate: String? = null,
    @Column(name = "premium_amount")
    val premiumAmount: String? = null,
    @Column(name = "recipient_email", nullable = false)
    val recipientEmail: String,
    @Column(name = "recipient_phone", nullable = false)
    val recipientPhone: String,
    @Column(name = "recipient_user_id")
    val recipientUserId: String? = null,
    @CreatedDate
    @Column(name = "create_date", nullable = false, updatable = false)
    val createDate: Instant? = null,
    @LastModifiedDate
    @Column(name = "update_date", nullable = false)
    var updateDate: Instant? = null,
)
