package com.se347.analysticservice.domains.events.revenue;

import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.se347.analysticservice.domains.events.DomainEvent;

/**
 * Event published when a new daily revenue record is created.
 * Simplified: No commission tracking, just total revenue.
 */
@Value
public class DailyRevenueCreatedEvent implements DomainEvent {
    
    UUID eventId;
    UUID dailyRevenueId;
    LocalDate date;
    BigDecimal totalRevenue;
    Long totalTransactions;
    LocalDateTime occurredAt;
    
    public static DailyRevenueCreatedEvent now(
        UUID dailyRevenueId,
        LocalDate date,
        BigDecimal totalRevenue,
        Long totalTransactions
    ) {
        return new DailyRevenueCreatedEvent(
            UUID.randomUUID(),
            dailyRevenueId,
            date,
            totalRevenue,
            totalTransactions,
            LocalDateTime.now()
        );
    }
}
