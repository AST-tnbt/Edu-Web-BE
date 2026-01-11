package com.se347.userservice.publisher.impl;

import com.se347.userservice.publisher.UserProfileEventPublisher;
import com.se347.userservice.dtos.UserProfileCompletedEvent;
import com.se347.userservice.entities.UserProfile;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Service
public class UserProfileEventPublisherImpl implements UserProfileEventPublisher {

    @Autowired
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.user-profile}")
    private String userProfileExchangeName;

    @Value("${app.rabbitmq.routing-key.user-profile.completed}")
    private String userProfileCompletedRoutingKey;

    public UserProfileEventPublisherImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserProfileCompletedEvent(UserProfile userProfile) {
        
        UserProfileCompletedEvent userProfileCompletedEvent = UserProfileCompletedEvent.builder()
                .userId(userProfile.getUserId())
                .build();
        rabbitTemplate.convertAndSend(userProfileExchangeName, 
                                    userProfileCompletedRoutingKey, 
                                    userProfileCompletedEvent);
    }
}

