package com.se347.authservice.listenners.impl;

import com.se347.authservice.dtos.UserProfileCompletedEvent;
import com.se347.authservice.services.AuthenticationService;
import com.se347.authservice.listenners.UserProfileListenner;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import java.io.IOException;

@Service
public class UserProfileListennerImpl implements UserProfileListenner{

    @Autowired
    private final AuthenticationService authenticationService;

    public UserProfileListennerImpl(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.user-profile-completed}", containerFactory = "rabbitListenerContainerFactory")
    public void handleProfileCompleted(UserProfileCompletedEvent userProfileCompletedEvent,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            if (userProfileCompletedEvent.getUserId() == null) {
                throw new RuntimeException("User ID is required");
            }
            
            authenticationService.setUserFirstLoginFalse(userProfileCompletedEvent.getUserId());
            
            // Acknowledge thành công
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            try {
                // Reject và không requeue
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                // Log error
                throw new RuntimeException("Failed to acknowledge message", ioException);
            }
        }
    }
}

