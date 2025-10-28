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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthenticationListenerServiceImpl implements AuthenticationListenerService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationListenerServiceImpl.class);

    @Autowired
    private final UserProfileService userProfileService;

    public AuthenticationListenerServiceImpl(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.user-created}", containerFactory = "SimpleRabbitListenerContainerFactory")
    public void handleUserCreatedEvent(UserCreatedEventDto userCreatedEvent, 
                                    Channel channel, 
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            boolean exists = userProfileService.existsByUserId(userCreatedEvent.getUserId());
            if (exists) {
                logger.info("Profile already exists for user: {}", userCreatedEvent.getUserId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            userProfileService.createProfileDefault(userCreatedEvent);
            logger.info("Successfully created profile default for user: {}", userCreatedEvent.getUserId());
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            try {
                channel.basicNack(deliveryTag, false, false);
                logger.error("Error creating profile default for user: {}", userCreatedEvent.getUserId(), e);
            } catch (IOException ioException) {
                logger.error("Error acknowledging message for user: {}", userCreatedEvent.getUserId(), ioException);
            }
        }
    }
}
