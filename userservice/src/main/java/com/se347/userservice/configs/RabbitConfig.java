package com.se347.userservice.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.exchange.auth}")
    private String authExchangeName;

    @Value("${app.rabbitmq.queue.user-created}")
    private String userCreatedQueueName;

    @Value("${app.rabbitmq.routing-key.user-created:user.created}")
    private String userCreatedRoutingKey;

    @Value("${app.rabbitmq.exchange.user-profile}")
    private String userProfileExchangeName;

    @Value("${app.rabbitmq.routing-key.user-profile.completed}")
    private String userProfileCompletedRoutingKey;

    @Value("${app.rabbitmq.queue.user-profile.completed}")
    private String userProfileCompletedQueueName;

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(authExchangeName, true, false);
    }

    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(userCreatedQueueName).build();
    }

    @Bean
    public Binding bindingUserCreated(Queue userCreatedQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(userCreatedQueue).to(authExchange).with(userCreatedRoutingKey);
    }

    @Bean
    public TopicExchange userProfileExchange() {
        return new TopicExchange(userProfileExchangeName, true, false);
    }

    @Bean
    public Queue userProfileCompletedQueue() {
        return new Queue(userProfileCompletedQueueName, true);
    }

    @Bean
    public Binding bindingUserProfileCompleted(Queue userProfileCompletedQueue, TopicExchange userProfileExchange) {
        return BindingBuilder.bind(userProfileCompletedQueue).to(userProfileExchange).with(userProfileCompletedRoutingKey);
    }

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

    // Listener Container Factory
    // Note: Using MANUAL acknowledgeMode because we handle ack/nack manually in listeners
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);  // Manual ack/nack
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}

