package ru.sogaz.site.orderingService.config

import com.fasterxml.jackson.databind.ObjectMapper
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
import ru.sogaz.site.orderingService.properties.RabbitProps


@Configuration
class RabbitConfig(
    private val props: RabbitProps,
    private val objectMapper: ObjectMapper) {

    @Bean
fun ordersQueue(): Queue = Queue(props.queue, true)

@Bean
fun ordersExchange(): TopicExchange = TopicExchange(props.exchange)

@Bean
fun ordersBinding(queue: Queue, exchange: TopicExchange): Binding =
    BindingBuilder.bind(queue).to(exchange).with(props.routingKey)

@Bean
fun messageConverter(): MessageConverter {
    return Jackson2JsonMessageConverter(objectMapper)
}

@Bean
fun rabbitTemplate(connectionFactory: ConnectionFactory, messageConverter: MessageConverter): RabbitTemplate {
    return RabbitTemplate(connectionFactory).apply {
        this.messageConverter = messageConverter
    }
}

@Bean
fun listenerFactory(
    connectionFactory: ConnectionFactory,
    messageConverter: MessageConverter
): SimpleRabbitListenerContainerFactory {
    return SimpleRabbitListenerContainerFactory().apply {
        setConnectionFactory(connectionFactory)
        setMessageConverter(messageConverter)
    }
}
}