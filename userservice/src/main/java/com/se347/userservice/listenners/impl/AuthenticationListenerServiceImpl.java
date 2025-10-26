package com.se347.userservice.listenners.impl;

import com.se347.userservice.listenners.AuthenticationListenerService;
import com.se347.userservice.services.UserProfileService;
import com.se347.userservice.dtos.UserCreatedEventDto;

import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class AuthenticationListenerServiceImpl implements AuthenticationListenerService {

    @Autowired
    private final UserProfileService userProfileService;

    public AuthenticationListenerServiceImpl(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.user-created}")
    public void handleUserCreatedEvent(UserCreatedEventDto userCreatedEvent, 
                                     Channel channel, 
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            // Xử lý message
            if (userProfileService.getProfileByUserId(userCreatedEvent.getUserId()) != null) {
                return;
            }
            userProfileService.createProfileDefault(userCreatedEvent);
            
            // Acknowledge thành công
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            try {
                // Reject và không requeue
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                // Log error
            }
        }
    }
}
