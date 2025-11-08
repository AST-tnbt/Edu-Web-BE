package com.se347.enrollmentservice.listeners;

import com.se347.enrollmentservice.dtos.events.PaymentCompletedEventDto;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;

public interface PaymentListener {
    void handlePaymentCompletedEvent(PaymentCompletedEventDto paymentCompletedEventDto,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag);
}
