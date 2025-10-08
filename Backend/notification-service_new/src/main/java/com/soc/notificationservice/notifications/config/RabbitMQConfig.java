package com.soc.notificationservice.notifications.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soc.notificationservice.notifications.ApplicationProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RabbitMQConfig {
    private final ApplicationProperties properties;

    RabbitMQConfig(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(properties.orderEventsExchange());
    }

    @Bean
    DirectExchange documentExchange() {
        return new DirectExchange(properties.documentEventsExchange());
    }

    @Bean
    Queue newOrdersQueue() {
        return QueueBuilder.durable(properties.newOrdersQueue()).build();
    }

    @Bean
    Binding newOrdersQueueBinding() {
        return BindingBuilder.bind(newOrdersQueue()).to(exchange()).with(properties.newOrdersQueue());
    }

    @Bean
    Queue deliveredOrdersQueue() {
        return QueueBuilder.durable(properties.deliveredOrdersQueue()).build();
    }

    @Bean
    Binding deliveredOrdersQueueBinding() {
        return BindingBuilder.bind(deliveredOrdersQueue()).to(exchange()).with(properties.deliveredOrdersQueue());
    }

    @Bean
    Queue cancelledOrdersQueue() {
        return QueueBuilder.durable(properties.cancelledOrdersQueue()).build();
    }

    @Bean
    Binding cancelledOrdersQueueBinding() {
        return BindingBuilder.bind(cancelledOrdersQueue()).to(exchange()).with(properties.cancelledOrdersQueue());
    }

    @Bean
    Queue errorOrdersQueue() {
        return QueueBuilder.durable(properties.errorOrdersQueue()).build();
    }

    @Bean
    Binding errorOrdersQueueBinding() {
        return BindingBuilder.bind(errorOrdersQueue()).to(exchange()).with(properties.errorOrdersQueue());
    }

    // Document queues and bindings
    @Bean
    Queue documentCreatedQueue() {
        return QueueBuilder.durable(properties.documentCreatedQueue()).build();
    }

    @Bean
    Binding documentCreatedQueueBinding() {
        return BindingBuilder.bind(documentCreatedQueue())
                .to(documentExchange())
                .with(properties.documentCreatedQueue());
    }

    @Bean
    Queue documentUpdatedQueue() {
        return QueueBuilder.durable(properties.documentUpdatedQueue()).build();
    }

    @Bean
    Binding documentUpdatedQueueBinding() {
        return BindingBuilder.bind(documentUpdatedQueue())
                .to(documentExchange())
                .with(properties.documentUpdatedQueue());
    }

    @Bean
    Queue documentApprovedQueue() {
        return QueueBuilder.durable(properties.documentApprovedQueue()).build();
    }

    @Bean
    Binding documentApprovedQueueBinding() {
        return BindingBuilder.bind(documentApprovedQueue())
                .to(documentExchange())
                .with(properties.documentApprovedQueue());
    }

    @Bean
    Queue documentRejectedQueue() {
        return QueueBuilder.durable(properties.documentRejectedQueue()).build();
    }

    @Bean
    Binding documentRejectedQueueBinding() {
        return BindingBuilder.bind(documentRejectedQueue())
                .to(documentExchange())
                .with(properties.documentRejectedQueue());
    }

    @Bean
    Queue documentErrorQueue() {
        return QueueBuilder.durable(properties.documentErrorQueue()).build();
    }

    @Bean
    Binding documentErrorQueueBinding() {
        return BindingBuilder.bind(documentErrorQueue()).to(documentExchange()).with(properties.documentErrorQueue());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        final var rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonConverter(objectMapper));
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }
}
