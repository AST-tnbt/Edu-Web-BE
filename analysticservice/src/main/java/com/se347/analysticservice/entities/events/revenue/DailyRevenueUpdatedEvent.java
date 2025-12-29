package com.se347.analysticservice.entities.events.revenue;

import com.se347.analysticservice.entities.events.DomainEvent;
import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when daily revenue metrics are updated.
 * Simplified: No commission/fee tracking.
 */
@Value
public class DailyRevenueUpdatedEvent implements DomainEvent {
    
    UUID eventId;
    UUID dailyRevenueId;
    BigDecimal newTotalRevenue;
    Long newTotalTransactions;
    LocalDateTime occurredAt;
    
    public static DailyRevenueUpdatedEvent now(
        UUID dailyRevenueId,
        BigDecimal newTotalRevenue,
        Long newTotalTransactions
    ) {
        return new DailyRevenueUpdatedEvent(
            UUID.randomUUID(),
            dailyRevenueId,
            newTotalRevenue,
            newTotalTransactions,
            LocalDateTime.now()
        );
    }
}

