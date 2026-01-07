package com.se347.analysticservice.listeners;

import com.rabbitmq.client.Channel;
import com.se347.analysticservice.dtos.events.payment.PaymentCompletedEvent;
import com.se347.analysticservice.services.RevenueAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Listener for Payment-related events from Payment Service.
 * Processes payment completion events to update revenue metrics.
 */
@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    
    private final RevenueAnalyticsService revenueAnalyticsService;
    
    /**
     * Handles PaymentCompletedEvent from Payment Service.
     * Updates daily revenue metrics.
     */
    @RabbitListener(
        queues = "${app.rabbitmq.queue.payment-completed}",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void handlePaymentCompleted(
        PaymentCompletedEvent event,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {
        try {
            // Validate event
            validatePaymentCompletedEvent(event);
            
            // Delegate to application services
            revenueAnalyticsService.recordRevenue(
                event.getAmount(),
                event.getCompletedAt().toLocalDate()
            );
            
            // Acknowledge success
            acknowledgeMessage(channel, deliveryTag);
        } catch (IllegalArgumentException e) {
            // Validation errors - reject without requeue (poison message)
            rejectMessage(channel, deliveryTag, false);
            
        } catch (Exception e) {
            // Business/infrastructure errors - reject WITH requeue (transient error)
            rejectMessage(channel, deliveryTag, true);
        }
    }
    
    // ==================== Validation Methods ====================
    
    private void validatePaymentCompletedEvent(PaymentCompletedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("PaymentCompletedEvent cannot be null");
        }
        if (event.getPaymentId() == null) {
            throw new IllegalArgumentException("PaymentId cannot be null in PaymentCompletedEvent");
        }
        if (event.getAmount() == null) {
            throw new IllegalArgumentException("Amount cannot be null in PaymentCompletedEvent");
        }
        if (event.getCompletedAt() == null) {
            throw new IllegalArgumentException("CompletedAt cannot be null in PaymentCompletedEvent");
        }
    }
    
    // ==================== Message Acknowledgment Methods ====================
    
    private void acknowledgeMessage(Channel channel, long deliveryTag) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.basicAck(deliveryTag, false);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to acknowledge message", e);
        }
    }
    
    private void rejectMessage(Channel channel, long deliveryTag, boolean requeue) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.basicNack(deliveryTag, false, requeue);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to reject message", e);
        }
    }
}

