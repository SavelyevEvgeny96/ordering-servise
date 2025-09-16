package ru.sogaz.site.orderingService.entity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "client_systems")
data class ClientSystem(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long? = null,

    @Column(name = "external_system_code", unique = true, length = 50)
    val externalSystemCode: String,

    @Column(name = "external_system_name", nullable = false)
    val externalSystemName: String
)