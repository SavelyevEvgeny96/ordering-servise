package ru.sogaz.site.orderingService.dto

data class OrderMessageDto(
    val orderId: String,              // идентификатор заказа
    val eventType: String,            // тип события ("order_created")
    val timestamp: String,            // время создания
    val author: String,               // источник события
    val externalSystemCode: String,   // код внешней системы (обязателен)
    val recipientEmail: String?,      // email страхователя
    val recipientPhone: String?,      // телефон страхователя
    val recipientUserId: String?,     // ID личного кабинета
    val recipientGdId: String?,       // золотой ID
    val recurrent: Boolean = false,   // рекуррентный платёж?
    val keyCard: String?,             // ключ карты (если recurrent=true)
    val saveCard: Boolean? = null,    // сохранять карту?
    val managerEmail: String?,        // менеджер по заказу
    val paymentEndDate: String?,      // срок актуальности заказа
    val payments: List<PaymentDto>    // список полисов внутри заказа
)