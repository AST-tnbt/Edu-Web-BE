package com.se347.paymentservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCompletedEvent {
    private UUID userId;
    private UUID courseId;
    private String courseSlug;
}
