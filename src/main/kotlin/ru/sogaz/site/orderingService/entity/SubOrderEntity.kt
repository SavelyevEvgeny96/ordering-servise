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
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
@Entity
@Table(name = "sub_orders")
data class SubOrderEntity(

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "sub_order_id")
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id", nullable = false)
    var orderEntity: OrderEntity? = null,

    @Column(name = "operation_id")
    var operationId: String? = null,

    @Column(name = "doc_type")
    var docType: String? = null,

    @Column(name = "policy_id", nullable = false)
    var policyId: String,

    @Column(name = "policy_number", nullable = false)
    var policyNumber: String,

    @Column(name = "contract_id")
    var contractId: String? = null,

    @Column(name = "contract_number", nullable = false)
    var contractNumber: String,

    @Column(name = "insurance_program")
    var insuranceProgram: String? = null,

    @Column(name = "type_insurance")
    var typeInsurance: String? = null,

    @Column(name = "premium_amount", precision = 19, scale = 2)
    var premiumAmount: BigDecimal? = null,

    @Column(name = "manager_email")
    var managerEmail: String? = null,

    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: Instant? = null,

    @UpdateTimestamp
    @Column(name = "update_date")
    var updateDate: Instant? = null,
)