package com.se347.userservice.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.exchange.auth_user}")
    private String authAndUserExchangeName;

    @Value("${app.rabbitmq.queue.user-created}")
    private String userCreatedQueueName;

    @Value("${app.rabbitmq.routing-key.user-created}")
    private String userCreatedRoutingKey;

    @Value("${app.rabbitmq.queue.user-profile-completed}")
    private String userProfileCompletedQueueName;

    @Value("${app.rabbitmq.routing-key.user-profile-completed}")
    private String userProfileCompletedRoutingKey;

    @Bean
    public TopicExchange authUserExchange() {
        return new TopicExchange(authAndUserExchangeName, true, false);
    }

    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(userCreatedQueueName).build();
    }

    @Bean
    public Binding bindingUserCreated(Queue userCreatedQueue, TopicExchange authUserExchange) {
        return BindingBuilder.bind(userCreatedQueue).to(authUserExchange).with(userCreatedRoutingKey);
    }

    @Bean
    public Queue userProfileCompletedQueue() {
        return new Queue(userProfileCompletedQueueName, true);
    }

    @Bean
    public Binding bindingUserProfileCompleted(Queue userProfileCompletedQueue, TopicExchange authUserExchange) {
        return BindingBuilder.bind(userProfileCompletedQueue).to(authUserExchange).with(userProfileCompletedRoutingKey);
    }

    // JSON serializer for messages
    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}

