package com.se347.analysticservice.listeners;

import com.rabbitmq.client.Channel;
import com.se347.analysticservice.dtos.events.payment.PaymentCompletedEvent;
import com.se347.analysticservice.services.admin.RevenueAnalyticsService;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
/**
 * Listener for Payment-related events from Payment Service.
 * Processes payment completion events to update revenue metrics.
 */

@Slf4j
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
        log.info("🔔 [PAYMENT] Received PaymentCompletedEvent - DeliveryTag: {}, Event: {}", 
            deliveryTag, event != null ? event.toString() : "NULL");
        
        if (event == null) {
            log.error("❌ [PAYMENT] PaymentCompletedEvent is NULL - DeliveryTag: {}", deliveryTag);
            rejectMessage(channel, deliveryTag, false);
            return;
        }
        
        try {
            // Validate event
            validatePaymentCompletedEvent(event);
            
            log.info("✅ [PAYMENT] Validated PaymentCompletedEvent - PaymentId: {}, Amount: {}, CompletedAt: {}", 
                event.getPaymentId(), event.getAmount(), event.getCompletedAt());
            
            // Delegate to application services
            revenueAnalyticsService.recordRevenue(
                event.getAmount(),
                event.getCompletedAt().toLocalDate()
            );
            
            log.info("✅ [PAYMENT] Successfully recorded revenue - PaymentId: {}, Amount: {}", 
                event.getPaymentId(), event.getAmount());
            
            // Acknowledge success
            acknowledgeMessage(channel, deliveryTag);
        } catch (IllegalArgumentException e) {
            // Validation errors - reject without requeue (poison message)
            log.error("❌ [PAYMENT] Validation error - DeliveryTag: {}, Error: {}", deliveryTag, e.getMessage(), e);
            rejectMessage(channel, deliveryTag, false);
            
        } catch (Exception e) {
            // Business/infrastructure errors - reject WITH requeue (transient error)
            log.error("❌ [PAYMENT] Processing error - DeliveryTag: {}, Error: {}", deliveryTag, e.getMessage(), e);
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
                log.info("✅ [PAYMENT] Message acknowledged - DeliveryTag: {}", deliveryTag);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to acknowledge message", e);
        }
    }
    
    private void rejectMessage(Channel channel, long deliveryTag, boolean requeue) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.basicNack(deliveryTag, false, requeue);
                log.warn("⚠️ [PAYMENT] Message rejected - DeliveryTag: {}, Requeue: {}", deliveryTag, requeue);
            } else {
                log.warn("⚠️ [PAYMENT] Channel is closed, cannot reject message - DeliveryTag: {}", deliveryTag);
            }
        } catch (IOException e) {
            log.error("❌ [PAYMENT] Failed to reject message - DeliveryTag: {}, Error: {}", deliveryTag, e.getMessage(), e);
            throw new RuntimeException("Failed to reject message", e);
        }
    }
}

