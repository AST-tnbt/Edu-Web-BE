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
     * ID of the instructor receiving the payment (for revenue split).
     */
    private UUID instructorId;
    
    /**
     * Payment amount.
     */
    private BigDecimal amount;
    
    /**
     * Currency code (e.g., USD, VND).
     */
    private String currency;
    
    /**
     * Platform fee/commission amount.
     */
    private BigDecimal platformFee;
    
    /**
     * Instructor's earning (amount - platformFee).
     */
    private BigDecimal instructorEarning;
    
    /**
     * Payment method (CREDIT_CARD, PAYPAL, etc.).
     */
    private String paymentMethod;
    
    /**
     * Payment status (should be COMPLETED for this event).
     */
    private String status;
    
    /**
     * Timestamp when payment was completed.
     */
    private LocalDateTime completedAt;
    
    /**
     * Event occurrence timestamp.
     */
    private LocalDateTime occurredAt;
    
    /**
     * Event version for compatibility.
     */
    private Integer version;
}

