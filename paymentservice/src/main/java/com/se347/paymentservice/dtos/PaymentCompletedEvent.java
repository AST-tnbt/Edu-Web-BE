package com.se347.paymentservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCompletedEvent {
    private UUID eventId;
    private UUID paymentId;
    private UUID userId;
    private UUID courseId;
    private UUID instructorId;
    private BigDecimal amount;
    private String courseSlug;
    private String vnpTxnRef;
    private LocalDateTime completedAt;
    private LocalDateTime occurredAt;
}
