package com.se347.analysticservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;

@Configuration
public class RabbitMQConfig {
    
    @Value("${app.rabbitmq.exchange.auth}")
    private String authExchangeName;

    @Value("${app.rabbitmq.queue.user-created}")
    private String userCreatedQueueName;
    
    @Value("${app.rabbitmq.routing-key.user-created:user.created}")
    private String userCreatedRoutingKey;
    
    @Value("${app.rabbitmq.queue.user-login}")
    private String userLoginQueueName;

    @Value("${app.rabbitmq.routing-key.user-login:user.login}")
    private String userLoginRoutingKey;

    @Value("${app.rabbitmq.exchange.payment}")
    private String paymentExchangeName;

    @Value("${app.rabbitmq.routing-key.payment.completed}")
    private String paymentCompletedRoutingKey;

    @Value("${app.rabbitmq.queue.payment-completed}")
    private String paymentCompletedQueueName;

    @Value("${app.rabbitmq.exchange.course}")
    private String courseExchangeName;

    @Value("${app.rabbitmq.queue.course-created}")
    private String courseCreatedQueueName;
    
    @Value("${app.rabbitmq.routing-key.course-created:course-created}")
    private String courseCreatedRoutingKey;
    
    @Value("${app.rabbitmq.queue.course-published}")
    private String coursePublishedQueueName;

    @Value("${app.rabbitmq.routing-key.course-published:course-published}")
    private String coursePublishedRoutingKey;

    @Value("${app.rabbitmq.exchange.enrollment}")
    private String enrollmentExchangeName;

    @Value("${app.rabbitmq.queue.enrollment-created}")
    private String enrollmentCreatedQueueName;
    
    @Value("${app.rabbitmq.routing-key.enrollment-created:enrollment-created}")
    private String enrollmentCreatedRoutingKey;

    @Value("${app.rabbitmq.queue.enrollment-completed}")
    private String enrollmentCompletedQueueName;

    @Value("${app.rabbitmq.routing-key.enrollment-completed:enrollment-completed}")
    private String enrollmentCompletedRoutingKey;
    
    @Value("${app.rabbitmq.queue.update-overall-progress}")
    private String updateOverallProgressQueueName;

    @Value("${app.rabbitmq.routing-key.update-overall-progress:update-overall-progress}")
    private String updateOverallProgressRoutingKey;

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(authExchangeName, true, false);
    }

    @Bean
    public Queue userCreatedQueue() {
        return new Queue(userCreatedQueueName, true);
    }

    @Bean
    public Queue userLoginQueue() {
        return new Queue(userLoginQueueName, true);
    }

    @Bean
    public Binding bindingUserCreated(Queue userCreatedQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(userCreatedQueue).to(authExchange).with(userCreatedRoutingKey);
    }

    @Bean
    public Binding bindingUserLogin(Queue userLoginQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(userLoginQueue).to(authExchange).with(userLoginRoutingKey);
    }

    // Payment Exchange and Queue
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(paymentExchangeName, true, false);
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return new Queue(paymentCompletedQueueName, true);
    }

    @Bean
    public Binding bindingPaymentCompleted(Queue paymentCompletedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentCompletedQueue).to(paymentExchange).with(paymentCompletedRoutingKey);
    }

    // Course Exchange and Queue
    @Bean
    public TopicExchange courseExchange() {
        return new TopicExchange(courseExchangeName, true, false);
    }

    @Bean
    public Queue courseCreatedQueue() {
        return new Queue(courseCreatedQueueName, true);
    }
    @Bean
    public Binding bindingCourseCreated(Queue courseCreatedQueue, TopicExchange courseExchange) {
        return BindingBuilder.bind(courseCreatedQueue).to(courseExchange).with(courseCreatedRoutingKey);
    }
    
    @Bean
    public Queue coursePublishedQueue() {
        return new Queue(coursePublishedQueueName, true);
    }
    
    @Bean
    public Binding bindingCoursePublished(Queue coursePublishedQueue, TopicExchange courseExchange) {
        return BindingBuilder.bind(coursePublishedQueue).to(courseExchange).with(coursePublishedRoutingKey);
    }

    // Enrollment Exchange and Queue
    @Bean
    public TopicExchange enrollmentExchange() {
        return new TopicExchange(enrollmentExchangeName, true, false);
    }

    @Bean
    public Queue enrollmentCreatedQueue() {
        return new Queue(enrollmentCreatedQueueName, true);
    }
    
    @Bean
    public Binding bindingEnrollmentCreated(Queue enrollmentCreatedQueue, TopicExchange enrollmentExchange) {
        return BindingBuilder.bind(enrollmentCreatedQueue).to(enrollmentExchange).with(enrollmentCreatedRoutingKey);
    }
    
    @Bean
    public Queue enrollmentCompletedQueue() {
        return new Queue(enrollmentCompletedQueueName, true);
    }
    
    @Bean
    public Binding bindingEnrollmentCompleted(Queue enrollmentCompletedQueue, TopicExchange enrollmentExchange) {
        return BindingBuilder.bind(enrollmentCompletedQueue).to(enrollmentExchange).with(enrollmentCompletedRoutingKey);
    }
    
    @Bean
    public Queue updateOverallProgressQueue() {
        return new Queue(updateOverallProgressQueueName, true);
    }
    
    @Bean
    public Binding bindingUpdateOverallProgress(Queue updateOverallProgressQueue, TopicExchange enrollmentExchange) {
        return BindingBuilder.bind(updateOverallProgressQueue).to(enrollmentExchange).with(updateOverallProgressRoutingKey);
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
