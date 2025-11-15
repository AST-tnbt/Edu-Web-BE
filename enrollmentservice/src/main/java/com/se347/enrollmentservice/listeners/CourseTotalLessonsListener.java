package com.se347.enrollmentservice.listeners;

import com.se347.enrollmentservice.dtos.events.TotalLessonsEventDto;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;

public interface CourseTotalLessonsListener {
    void handleSetTotalLessonsEvent(TotalLessonsEventDto event,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag);
}

