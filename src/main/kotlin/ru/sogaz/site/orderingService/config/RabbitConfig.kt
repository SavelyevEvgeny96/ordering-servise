package ru.sogaz.site.orderingService.config
import org.springframework.util.ErrorHandler
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.orderingService.properties.RabbitListenerProps
import ru.sogaz.site.orderingService.properties.RabbitProps

@Configuration
class RabbitConfig(
    private val connectionFactory: ConnectionFactory,
    private val props: RabbitProps,
    private val propsListener: RabbitListenerProps
) {
    @Bean
    fun ordersExchange(): TopicExchange = TopicExchange(props.exchange, true, false)

    @Bean(name = ["ordersQueue"])
    fun ordersQueue(): Queue = Queue(props.queueOrder, true)

    @Bean(name = ["paymentsQueue"])
    fun paymentsQueue(): Queue = Queue(props.queuePayment, true)

    @Bean
    fun ordersBinding(
        @Qualifier("ordersQueue") queue: Queue,
        exchange: TopicExchange
    ): Binding = BindingBuilder.bind(queue).to(exchange).with(props.routingKeyOrder)

    @Bean
    fun paymentsBinding(
        @Qualifier("paymentsQueue") queue: Queue,
        exchange: TopicExchange
    ): Binding = BindingBuilder.bind(queue).to(exchange).with(props.routingKeyPayment)

    @Bean
    fun jacksonMessageConverter(objectMapper: ObjectMapper): MessageConverter =
        Jackson2JsonMessageConverter(objectMapper)

    @Bean
    fun rabbitTemplate(messageConverter: MessageConverter): RabbitTemplate =
        RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }

    @Bean("batchContainerFactory")
    fun batchContainerFactory(messageConverter: MessageConverter): SimpleRabbitListenerContainerFactory =
        SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(messageConverter)
            setBatchListener(true)
            setConsumerBatchEnabled(true)
            setDeBatchingEnabled(false)
            setConsumerBatchEnabled(true)
            setBatchSize(propsListener.batchSize)
            setPrefetchCount(propsListener.prefetch)
            setConcurrentConsumers(propsListener.concurrency)
            setMaxConcurrentConsumers(propsListener.maxConcurrency)
            setAcknowledgeMode(AcknowledgeMode.MANUAL)
            setChannelTransacted(true)
            setDefaultRequeueRejected(false)
        }
}