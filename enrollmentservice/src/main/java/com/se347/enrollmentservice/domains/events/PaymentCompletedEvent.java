package com.se347.enrollmentservice.domains.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: Payment was completed
 * 
 * WHEN RAISED:
 * - Payment status changes to PAID
 * 
 * WHO LISTENS:
 * - NotificationService: Send receipt
 * - AnalyticsService: Track revenue
 * - InvoiceService: Generate invoice
 */
@Getter
@RequiredArgsConstructor
public class PaymentCompletedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final UUID enrollmentId;
    private final UUID courseId;
    private final UUID studentId;
    private final LocalDateTime occurredAt;
    
    public static PaymentCompletedEvent now(
            UUID enrollmentId, 
            UUID courseId, 
            UUID studentId) {
        return new PaymentCompletedEvent(
                UUID.randomUUID(),
                enrollmentId, 
                courseId, 
                studentId, 
                LocalDateTime.now());
    }
}

