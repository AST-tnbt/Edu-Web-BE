package com.se347.analysticservice.dtos.events.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO received from Payment Service when a payment is successfully completed.
 * This is crucial for tracking platform revenue and instructor earnings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEvent {
    
    /**
     * Unique event identifier.
     */
    private UUID eventId;
    
    /**
     * ID of the payment transaction.
     */
    private UUID paymentId;
    
    /**
     * ID of the user who made the payment.
     */
    private UUID userId;
    
    /**
     * ID of the course being purchased (if applicable).
     */
    private UUID courseId;
    
    /**
     * ID of the instructor (if applicable).
     */
    private UUID instructorId;
    
    /**
     * Payment amount.
     */
    private BigDecimal amount;
    
    /**
     * Course slug (if applicable).
     */
    private String courseSlug;
    
    /**
     * VNPay transaction reference.
     */
    private String vnpTxnRef;
    
    /**
     * Payment completion timestamp.
     */
    private LocalDateTime completedAt;
    
    /**
     * Event occurrence timestamp.
     */
    private LocalDateTime occurredAt;
}

