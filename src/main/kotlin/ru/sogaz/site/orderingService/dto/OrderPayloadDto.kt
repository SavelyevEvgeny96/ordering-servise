package ru.sogaz.site.orderingService.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class OrderPayloadDto(
    @field:NotNull
    val metaInfo: List<MetaInfoOrder>,
    @field:NotBlank
    val orderId: String,
    @field:Email
    val recipientEmail: String?, // email страхователя
    val recipientPhone: String?, // телефон страхователя
    val recipientUserId: String?, // ID личного кабинета
    val recipientGdId: String?, // золотой ID
    val recurrent: Boolean = false, // рекуррентный платёж
    val keyCard: String?, // ключ карты (если recurrent=true)
    val saveCard: Boolean? = null, // сохранять карту?
    val managerEmail: String?, // менеджер по заказу
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    val orderEndDate: Instant?, // срок актуальности заказа
    val subOrders: List<SubOrderDto>, // список полисов внутри заказа
)
