package com.se347.authservice.listenners;

import com.se347.authservice.dtos.UserProfileCompletedEvent;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;

public interface UserProfileListenner {
    void handleProfileCompleted(UserProfileCompletedEvent userProfileCompletedEvent,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag);
}
