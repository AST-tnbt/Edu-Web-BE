package com.se347.analysticservice.domains.events.platform;

import lombok.Value;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.se347.analysticservice.domains.events.DomainEvent;

/**
 * Event published when user growth analytics for a date is recorded.
 */
@Value
public class UserGrowthRecordedEvent implements DomainEvent {
    
    UUID eventId;
    UUID userGrowthAnalyticsId;
    LocalDate date;
    Long newUsersCount;
    Long activeUsersCount;
    Long totalUsers;
    Double retentionRate;
    LocalDateTime occurredAt;
    
    public static UserGrowthRecordedEvent now(
        UUID userGrowthAnalyticsId,
        LocalDate date,
        Long newUsersCount,
        Long activeUsersCount,
        Long totalUsers,
        Double retentionRate
    ) {
        return new UserGrowthRecordedEvent(
            UUID.randomUUID(),
            userGrowthAnalyticsId,
            date,
            newUsersCount,
            activeUsersCount,
            totalUsers,
            retentionRate,
            LocalDateTime.now()
        );
    }
}
