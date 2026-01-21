package com.se347.enrollmentservice.publishers.impl;

import com.se347.enrollmentservice.publishers.EnrollmentPublisher;
import com.se347.enrollmentservice.domains.events.EnrollmentCreatedEvent;
import com.se347.enrollmentservice.domains.events.EnrollmentCompletedEvent;
import com.se347.enrollmentservice.domains.events.UpdateOverallProgressEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class EnrollmentPublisherImpl implements EnrollmentPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.enrollment}")
    private String enrollmentExchangeName;

    @Value("${app.rabbitmq.routing-key.enrollment-created}")
    private String enrollmentCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.enrollment-completed}")
    private String enrollmentCompletedRoutingKey;

    @Value("${app.rabbitmq.routing-key.update-overall-progress}")
    private String updateOverallProgressRoutingKey;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEnrollmentCreatedEvent(EnrollmentCreatedEvent event) {
        rabbitTemplate.convertAndSend(enrollmentExchangeName, 
                                    enrollmentCreatedRoutingKey, 
                                    event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEnrollmentCompletedEvent(EnrollmentCompletedEvent event) {
        rabbitTemplate.convertAndSend(enrollmentExchangeName, 
                                    enrollmentCompletedRoutingKey, 
                                    event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishUpdateOverallProgressEvent(UpdateOverallProgressEvent event) {
        rabbitTemplate.convertAndSend(enrollmentExchangeName, 
                                    updateOverallProgressRoutingKey, 
                                    event);
                                    log.info("Published UpdateOverallProgressEvent: {}", event);
    }
}