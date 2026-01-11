package com.se347.enrollmentservice.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.exchange.payment}")
    private String paymentExchangeName;

    @Value("${app.rabbitmq.queue.payment-completed}")
    private String paymentCompletedQueueName;

    @Value("${app.rabbitmq.exchange.enrollment-course}")
    private String enrollmentCourseExchangeName;

    @Value("${app.rabbitmq.routing-key.set-total-lessons}")
    private String setTotalLessonsRoutingKey;

    @Value("${app.rabbitmq.queue.set-total-lessons}")
    private String setTotalLessonsQueueName;

    @Value("${app.rabbitmq.exchange.enrollment}")
    private String enrollmentExchangeName;

    @Value("${app.rabbitmq.queue.enrollment-created}")
    private String enrollmentCreatedQueueName;

    @Value("${app.rabbitmq.routing-key.enrollment-created}")
    private String enrollmentCreatedRoutingKey;

    @Bean
    public TopicExchange enrollmentPaymentExchange() {
        return new TopicExchange(paymentExchangeName, true, false);
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return new Queue(paymentCompletedQueueName, true);
    }
    
    @Bean
    public TopicExchange enrollmentCourseExchange() {
        return new TopicExchange(enrollmentCourseExchangeName, true, false);
    }

    @Bean
    public Queue setTotalLessonsQueue() {
        return QueueBuilder.durable(setTotalLessonsQueueName).build();
    }
    
    @Bean
    public Binding bindingSetTotalLessons(Queue setTotalLessonsQueue, TopicExchange enrollmentCourseExchange) {
        return BindingBuilder.bind(setTotalLessonsQueue).to(enrollmentCourseExchange).with(setTotalLessonsRoutingKey);
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
