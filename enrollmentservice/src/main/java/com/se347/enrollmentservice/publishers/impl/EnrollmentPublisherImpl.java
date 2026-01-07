package com.se347.enrollmentservice.publishers.impl;

import com.se347.enrollmentservice.publishers.EnrollmentPublisher;
import com.se347.enrollmentservice.domains.events.EnrollmentCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EnrollmentPublisherImpl implements EnrollmentPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.enrollment}")
    private String enrollmentExchangeName;

    @Value("${app.rabbitmq.routing-key.enrollment-created}")
    private String enrollmentCreatedRoutingKey;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEnrollmentCreatedEvent(EnrollmentCreatedEvent event) {
        rabbitTemplate.convertAndSend(enrollmentExchangeName, 
                                    enrollmentCreatedRoutingKey, 
                                    event);
    }
}