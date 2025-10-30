package com.se347.authservice.publishers.impl;

import com.se347.authservice.dtos.UserCreatedEventDto;
import com.se347.authservice.publishers.AuthenticationEventPublisher;
import com.se347.authservice.entities.User;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationEventPublisherImpl implements AuthenticationEventPublisher {

    @Autowired
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.auth_user}")
    private String authAndUserExchangeName;

    @Value("${app.rabbitmq.routing-key.user-created}")
    private String userCreatedRoutingKey;

    public AuthenticationEventPublisherImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserCreatedEvent(User user) {
        UserCreatedEventDto userCreatedEvent = UserCreatedEventDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .build();
        rabbitTemplate.convertAndSend(authAndUserExchangeName, 
                                    userCreatedRoutingKey, 
                                    userCreatedEvent);
    }
}
