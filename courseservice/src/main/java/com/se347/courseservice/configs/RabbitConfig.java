package com.se347.courseservice.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.exchange.enrollment-course}")
    private String enrollmentCourseExchangeName;

    @Value("${app.rabbitmq.routing-key.set-total-lessons}")
    private String setTotalLessonsRoutingKey;

    @Value("${app.rabbitmq.queue.set-total-lessons}")
    private String setTotalLessonsQueueName;

    @Bean
    public TopicExchange enrollmentCourseExchange() {
        return new TopicExchange(enrollmentCourseExchangeName, true, false);
    }

    @Bean
    public Queue setTotalLessonsQueue() {
        return new Queue(setTotalLessonsQueueName, true);
    }

    @Bean
    public Binding bindingSetTotalLessons(Queue setTotalLessonsQueue, TopicExchange enrollmentCourseExchange) {
        return BindingBuilder.bind(setTotalLessonsQueue).to(enrollmentCourseExchange).with(setTotalLessonsRoutingKey);
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
