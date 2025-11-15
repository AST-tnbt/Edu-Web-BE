package com.se347.paymentservice.publisher;

import com.se347.paymentservice.dtos.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentPubliserImpl implements PaymentPublisher{

    private static final Logger logger = LoggerFactory.getLogger(PaymentPubliserImpl.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.enrollment_payment}")
    private String paymentExchangeName;

    @Value("${app.rabbitmq.routing-key.payment.completed}")
    private String paymentRoutingKey;

    @Override
    public void publishPaymentSuccessEvent(PaymentCompletedEvent paymentCompletedEvent) {
        logger.info("[Payment -> Enrollment] Publishing CustomerProfileRequestedEvent: {}", paymentCompletedEvent.toString());
        rabbitTemplate.convertAndSend(paymentExchangeName,
                paymentRoutingKey,
                paymentCompletedEvent);
    }
}
