package ru.sogaz.site.orderingService.dto

import java.math.BigDecimal

data class PaymentDto(
    val premiumAmount: BigDecimal,    // сумма премии (обязательно)
    val policyId: String?,            // идентификатор полиса
    val policyNumber: String?,        // номер полиса
    val contractNumber: String?,      // идентификатор договора
    val contractId: String?,          // номер договора
    val typeInsurance: String?,       // вид страхования (ОСАГО, КАСКО и т.д.)
    val insuranceProgram: String?     // программа страхования
)