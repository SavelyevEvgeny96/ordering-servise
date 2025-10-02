package ru.sogaz.site.orderingService.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.orderingService.dto.OrderPayloadDto
import ru.sogaz.site.orderingService.dto.SubOrderDto
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import java.time.Instant
import java.util.UUID

@Mapper(componentModel = "spring", imports = [UUID::class, Instant::class])
interface OrderMapper {

    @Mapping(target = "orderId", expression = "java(UUID.fromString(dto.orderId))")
    @Mapping(target = "recipientEmail", expression = "java(dto.getRecipientEmail() != null ? dto.getRecipientEmail() : \"\")")
    @Mapping(target = "recipientPhone", expression = "java(dto.getRecipientPhone() != null ? dto.getRecipientPhone() : \"\")")
    @Mapping(target = "paymentEndDate", source = "orderEndDate")
    @Mapping(target = "createDate", expression = "java(Instant.now())")
    fun toOrderEntity(dto: OrderPayloadDto): OrderEntity

    @Mapping(target = "orderEntity", source = "order")
    @Mapping(target = "managerEmail", source = "managerEmail")
    fun toSubOrderEntity(dto: SubOrderDto, order: OrderEntity, managerEmail: String?): SubOrderEntity

    fun toSubOrderEntities(dtos: List<SubOrderDto>, order: OrderEntity, managerEmail: String?): List<SubOrderEntity>
}