package ru.sogaz.site.orderingService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "payment_operation_history")
@EntityListeners(AuditingEntityListener::class)
data class PaymentOperationHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    val id: UUID? = null,
    @Column(name = "action")
    val action: Long? = null,
    @CreatedDate
    @Column(name = "action_date")
    val actionDate: Instant? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_author_id", referencedColumnName = "external_system_code")
    val actionAuthor: ClientSystemEntity? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    val orderEntity: OrderEntity? = null,
)
