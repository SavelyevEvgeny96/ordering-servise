import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import ru.sogaz.site.orderingService.dto.OrdersUserRequest
import ru.sogaz.site.orderingService.entity.OrderEntity
import ru.sogaz.site.orderingService.entity.SubOrderEntity
import ru.sogaz.site.orderingService.enums.OrderStatusesEnum
import ru.sogaz.site.orderingService.repository.OrderRepository
import ru.sogaz.site.orderingService.repository.SubOrderRepository
import ru.sogaz.site.orderingService.service.impl.OrdersUserServiceImpl
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrdersUserServiceImplTest {
    @Mock
    lateinit var orderRepository: OrderRepository

    @Mock
    lateinit var subOrderRepository: SubOrderRepository

    @InjectMocks
    lateinit var service: OrdersUserServiceImpl

    private lateinit var orderEntity: OrderEntity
    private lateinit var subOrderEntity: SubOrderEntity

    @BeforeEach
    fun setup() {
        val orderId = UUID.randomUUID()
        orderEntity =
            OrderEntity(
                orderId = orderId,
                recipientEmail = "client@mail.com",
                recipientPhone = "+79998887766",
                premiumAmount = BigDecimal.TEN,
                paymentEndDate = Instant.parse("2024-01-01T00:00:00Z"),
                status = OrderStatusesEnum.NEW,
                recurrent = false,
                keyCard = null,
                saveCard = true,
                recipientUserId = "user-1",
                recipientUserGdId = "gd-1",
            )

        subOrderEntity =
            SubOrderEntity(
                orderEntity = orderEntity,
                policyId = "policy-1",
                policyNumber = "PN-1",
                contractId = "CID-1",
                contractNumber = "CN-1",
                insuranceProgram = "Life",
                typeInsurance = "Personal",
                premiumAmount = BigDecimal.ONE,
                managerEmail = "manager@mail.com",
            )
    }

    @Test
    fun `должен вернуть список заказов по email`() {
        whenever(orderRepository.findAll(any())).thenReturn(listOf(orderEntity))
        whenever(subOrderRepository.findByOrderEntityOrderIdIn(any())).thenReturn(listOf(subOrderEntity))

        val request =
            OrdersUserRequest(
                state = "NEW",
                searchName = "email",
                email = "client@mail.com",
            )

        val result = service.findOrders(request)

        assertEquals("NEW", result.state)
        assertEquals(1, result.total)
        val order = result.orders.first()
        assertEquals(orderEntity.orderId, order.orderId)
        assertEquals(orderEntity.recipientEmail, order.recipientEmail)
        assertEquals(1, order.subOrders.size)
        assertEquals(subOrderEntity.policyId, order.subOrders.first().policyId)
    }

    @Test
    fun `должен вернуть пустой список, если заказы не найдены`() {
        whenever(orderRepository.findAll(any())).thenReturn(emptyList())

        val request =
            OrdersUserRequest(
                state = "NEW",
                searchName = "email",
                email = "client@mail.com",
            )

        val result = service.findOrders(request)

        assertEquals("NEW", result.state)
        assertEquals(0, result.total)
        assertTrue(result.orders.isEmpty())
        verify(subOrderRepository, never()).findByOrderEntityOrderIdIn(any())
    }

    @Test
    fun `должен выбросить исключение при отсутствии обязательного параметра`() {
        val request = OrdersUserRequest(state = "NEW", searchName = "email")

        val exception = assertThrows<IllegalArgumentException> { service.findOrders(request) }

        assertTrue(exception.message?.contains("email") == true)
    }

    @Test
    fun `должен выбросить исключение при некорректном uuid`() {
        val request =
            OrdersUserRequest(
                state = "NEW",
                searchName = "guid",
                uuid = "not-uuid",
            )

        val exception = assertThrows<IllegalArgumentException> { service.findOrders(request) }

        assertTrue(exception.message?.contains("uuid") == true)
    }
}
