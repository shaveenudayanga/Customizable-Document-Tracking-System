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

    // Workflow Exchange
    @Bean
    DirectExchange workflowExchange() {
        return new DirectExchange(properties.workflowEventsExchange());
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

    // Workflow queues and bindings
    @Bean
    Queue workflowStartedQueue() {
        return QueueBuilder.durable(properties.workflowStartedQueue()).build();
    }

    @Bean
    Binding workflowStartedQueueBinding() {
        return BindingBuilder.bind(workflowStartedQueue())
                .to(workflowExchange())
                .with(properties.workflowStartedQueue());
    }

    @Bean
    Queue taskCompletedQueue() {
        return QueueBuilder.durable(properties.taskCompletedQueue()).build();
    }

    @Bean
    Binding taskCompletedQueueBinding() {
        return BindingBuilder.bind(taskCompletedQueue()).to(workflowExchange()).with(properties.taskCompletedQueue());
    }

    @Bean
    Queue workflowCompletedQueue() {
        return QueueBuilder.durable(properties.workflowCompletedQueue()).build();
    }

    @Bean
    Binding workflowCompletedQueueBinding() {
        return BindingBuilder.bind(workflowCompletedQueue())
                .to(workflowExchange())
                .with(properties.workflowCompletedQueue());
    }

    @Bean
    Queue workflowRejectedQueue() {
        return QueueBuilder.durable(properties.workflowRejectedQueue()).build();
    }

    @Bean
    Binding workflowRejectedQueueBinding() {
        return BindingBuilder.bind(workflowRejectedQueue())
                .to(workflowExchange())
                .with(properties.workflowRejectedQueue());
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
