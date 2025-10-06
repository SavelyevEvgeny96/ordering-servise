import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.sogaz.site.orderingService.controller.OrdersUserController
import ru.sogaz.site.orderingService.dto.OrdersUserData
import ru.sogaz.site.orderingService.dto.OrdersUserOrder
import ru.sogaz.site.orderingService.dto.OrdersUserRequest
import ru.sogaz.site.orderingService.dto.OrdersUserSubOrder
import ru.sogaz.site.orderingService.service.OrdersUserService
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@ExtendWith(SpringExtension::class)
@WebMvcTest(OrdersUserController::class)
class OrdersUserControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var ordersUserService: OrdersUserService

    @Test
    fun `должен вернуть 200 и список заказов`() {
        val orderId = UUID.randomUUID()
        val data =
            OrdersUserData(
                state = "NEW",
                total = 1,
                orders =
                    listOf(
                        OrdersUserOrder(
                            orderId = orderId,
                            status = "NEW",
                            paymentEndDate = Instant.parse("2024-01-01T00:00:00Z"),
                            premiumAmount = BigDecimal.TEN,
                            recipientEmail = "client@mail.com",
                            recipientPhone = "+7999000000",
                            recurrent = false,
                            keyCard = null,
                            saveCard = true,
                            subOrders =
                                listOf(
                                    OrdersUserSubOrder(
                                        policyId = "policy",
                                        policyNumber = "PN-1",
                                        contractId = "CID",
                                        contractNumber = "CN",
                                        insuranceProgram = "Life",
                                        typeInsurance = "Personal",
                                        premiumAmount = BigDecimal.ONE,
                                        managerEmail = "manager@mail.com",
                                    ),
                                ),
                        ),
                    ),
            )

        Mockito.`when`(ordersUserService.findOrders(Mockito.any())).thenReturn(data)

        val request =
            OrdersUserRequest(
                state = "NEW",
                searchName = "email",
                email = "client@mail.com",
            )

        mockMvc
            .perform(
                post("/v1/orders/ordersuser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("TraceId", "trace-1")
                    .content(objectMapper.writeValueAsString(request)),
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.orders[0].orderId").value(orderId.toString()))
    }

    @Test
    fun `должен вернуть 422 при ошибке валидации`() {
        Mockito.`when`(ordersUserService.findOrders(Mockito.any())).thenThrow(IllegalArgumentException("Не заполнено обязательное значение: email"))

        val request =
            OrdersUserRequest(
                state = "NEW",
                searchName = "email",
            )

        mockMvc
            .perform(
                post("/v1/orders/ordersuser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.status").value("ERROR"))
            .andExpect(jsonPath("$.code").value(422))
            .andExpect(jsonPath("$.message").value("Не заполнено обязательное значение: email"))
    }

    @Test
    fun `должен вернуть 500 при внутренней ошибке`() {
        Mockito.`when`(ordersUserService.findOrders(Mockito.any())).thenThrow(RuntimeException("db error"))

        val request =
            OrdersUserRequest(
                state = "NEW",
                searchName = "email",
                email = "client@mail.com",
            )

        mockMvc
            .perform(
                post("/v1/orders/ordersuser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value("ERROR"))
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("Внутренняя ошибка сервера"))
    }
}
