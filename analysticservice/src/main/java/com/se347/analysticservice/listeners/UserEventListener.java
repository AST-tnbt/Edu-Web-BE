package com.se347.analysticservice.listeners;

import com.rabbitmq.client.Channel;
import com.se347.analysticservice.dtos.events.user.UserLoginEvent;
import com.se347.analysticservice.dtos.events.user.UserRegisteredEvent;
import com.se347.analysticservice.services.admin.PlatformOverviewService;
import com.se347.analysticservice.services.admin.UserGrowthAnalyticsService;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserEventListener {
    
    private final UserGrowthAnalyticsService userGrowthAnalyticsService;
    private final PlatformOverviewService platformOverviewService;
    @RabbitListener(
        queues = "${app.rabbitmq.queue.user-created}",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleUserRegistered(
        UserRegisteredEvent event,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {
        
        try {
            // Validate event
            validateUserRegisteredEvent(event);
            
            // Delegate to application service (transaction boundary)
            userGrowthAnalyticsService.recordUserRegistration(
                event.getUserId(),
                event.getCreatedAt().toLocalDate()
            );
            
            // Acknowledge success
            acknowledgeMessage(channel, deliveryTag, "Successfully processed");
            
        } catch (IllegalArgumentException e) {
            // Validation errors - reject without requeue (poison message)
            rejectMessage(channel, deliveryTag, false);
            
        } catch (Exception e) {
            // Business/infrastructure errors - reject WITH requeue (transient error)
            rejectMessage(channel, deliveryTag, true);
        }
    }
    
    @RabbitListener(
        queues = "${app.rabbitmq.queue.user-login}",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleUserLogin(
        UserLoginEvent event,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {
        try {
            // Validate event
            validateUserLoginEvent(event);
            
            // Delegate to application service
            userGrowthAnalyticsService.recordUserActivity(
                event.getUserId(),
                event.getLoginAt().toLocalDate()
            );
            
            // Acknowledge success
            acknowledgeMessage(channel, deliveryTag, "Successfully processed");
            
        } catch (IllegalArgumentException e) {
            // Validation errors - reject without requeue
            rejectMessage(channel, deliveryTag, false);
            
        } catch (Exception e) {
            // Processing errors - reject with requeue
            rejectMessage(channel, deliveryTag, true);
        }
    }
    
    // ==================== Validation Methods ====================
    
    private void validateUserRegisteredEvent(UserRegisteredEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("UserRegisteredEvent cannot be null");
        }
        if (event.getUserId() == null) {
            throw new IllegalArgumentException("UserId cannot be null in UserRegisteredEvent");
        }
    }
    
    private void validateUserLoginEvent(UserLoginEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("UserLoginEvent cannot be null");
        }
        if (event.getUserId() == null) {
            throw new IllegalArgumentException("UserId cannot be null in UserLoginEvent");
        }
        if (event.getLoginAt() == null) {
            throw new IllegalArgumentException("LoginAt cannot be null in UserLoginEvent");
        }
    }
    
    // ==================== Message Acknowledgment Methods ====================
    
    private void acknowledgeMessage(Channel channel, long deliveryTag, String reason) {
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

