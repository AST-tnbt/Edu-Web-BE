package com.se347.userservice.listenners;

import com.se347.userservice.dtos.UserCreatedEventDto;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;

public interface AuthenticationListenerService {
    void handleUserCreatedEvent(UserCreatedEventDto userCreatedEvent, 
                                Channel channel,        
                                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag);
}
