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
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sub_orders")
@EntityListeners(AuditingEntityListener::class)
data class SubOrder(

    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "sub_order_id", columnDefinition = "BINARY(16)")
    val id: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id", unique = true)
    val order: Order? = null,

    @Column(name = "operation_id")
    val operationId: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_system_code", referencedColumnName = "external_system_code")
    val externalSystem: ClientSystem? = null,

    @Column(name = "doc_type")
    val docType: String? = null,

    @Column(name = "policy_id", nullable = false)
    val policyId: String,

    @Column(name = "policy_number", nullable = false)
    val policyNumber: String,

    @Column(name = "contract_id")
    val contractId: String? = null,

    @Column(name = "contract_number", nullable = false)
    val contractNumber: String,

    @Column(name = "insurance_program")
    val insuranceProgram: String? = null,

    @Column(name = "type_insurance")
    val typeInsurance: String? = null,

    @Column(name = "premium_amount")
    val premiumAmount: String? = null,

    @Column(name = "manager_email")
    val managerEmail: String? = null,

    @CreatedDate
    @Column(name = "create_date", nullable = false, updatable = false)
    val createDate: Instant? = null,

    @LastModifiedDate
    @Column(name = "update_date", nullable = false)
    var updateDate: Instant? = null
)