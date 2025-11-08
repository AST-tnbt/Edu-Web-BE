package com.se347.paymentservice.publisher;

import com.se347.paymentservice.dtos.PaymentCompletedEvent;

public interface PaymentPublisher {
    public void publishPaymentSuccessEvent(PaymentCompletedEvent paymentCompletedEvent);
}
