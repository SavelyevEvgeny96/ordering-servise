package ru.sogaz.site.orderingService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.AcknowledgeMode
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.orderingService.properties.RabbitListenerProps
import ru.sogaz.site.orderingService.properties.RabbitProps

@Configuration
class RabbitConfig(
    private val connectionFactory: ConnectionFactory,
    private val props: RabbitProps,
    private val propsListener: RabbitListenerProps,
) {
    @Bean
    fun ordersQueue(): Queue = Queue(props.queue, true)

    @Bean
    fun ordersExchange(): TopicExchange = TopicExchange(props.exchange)

    @Bean
    fun ordersBinding(
        queue: Queue,
        exchange: TopicExchange,
    ): Binding = BindingBuilder.bind(queue).to(exchange).with(props.routingKey)

    @Bean
    fun jacksonMessageConverter(objectMapper: ObjectMapper): MessageConverter = Jackson2JsonMessageConverter(objectMapper)

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

            setBatchSize(propsListener.batchSize)
            setPrefetchCount(propsListener.prefetch)
            setConcurrentConsumers(propsListener.concurrency)
            setMaxConcurrentConsumers(propsListener.maxConcurrency)

            setAcknowledgeMode(AcknowledgeMode.AUTO)
            setReceiveTimeout(propsListener.receiveTimeoutMs)
        }
}
