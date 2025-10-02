package ru.sogaz.site.orderingService.mappers

import org.mapstruct.Context
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.PaymentCreatedEvent
import ru.sogaz.site.orderingService.dto.PaymentData
import ru.sogaz.site.orderingService.entity.OrderEntity
import java.time.ZoneOffset

@Mapper(componentModel = "spring", imports = [ZoneOffset::class])
interface PaymentEventMapper {
    @Mapping(target = "timestamp", source = "nowIso")
    @Mapping(target = "eventType", source = "eventType")
    @Mapping(target = "data", source = "order")
    fun toPaymentEvent(order: OrderEntity?, nowIso: String?, eventType: String?): PaymentCreatedEvent?

    @Mapping(target = "recurrent", expression = "java(order.getRecurrent() != null ? order.getRecurrent() : false)")
    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "premiumAmount", source = "premiumAmount")
    @Mapping(target = "saveCard", source = "saveCard")
    @Mapping(target = "keyCard", source = "keyCard")
    @Mapping(target = "recipientEmail", source = "recipientEmail")
    @Mapping(target = "recipientPhone", source = "recipientPhone")
    @Mapping(
        target = "dateCreate",
        expression = "java(order.getUpdateDate() != null ? order.getUpdateDate().atOffset(ZoneOffset.UTC).toString() : null)"
    )
    @Mapping(
        target = "dateEnd",
        expression = "java(order.getPaymentEndDate() != null ? order.getPaymentEndDate().atOffset(ZoneOffset.UTC).toString() : null)"
    )
    fun toPaymentData(order: OrderEntity): PaymentData

    fun toPaymentEvents(
        orders: List<OrderEntity>,
        @Context nowIso: String?,
        @Context eventType: String?
    ): List<PaymentCreatedEvent>
}