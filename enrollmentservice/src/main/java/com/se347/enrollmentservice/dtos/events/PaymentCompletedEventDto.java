package com.se347.enrollmentservice.dtos.events;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEventDto implements Serializable {
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
