package com.se347.analysticservice.entities.events.platform;

import com.se347.analysticservice.entities.events.DomainEvent;
import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when user metrics are updated during the day.
 * Used for real-time updates to dashboards.
 */
@Value
public class UserMetricsUpdatedEvent implements DomainEvent {
    
    UUID eventId;
    UUID userGrowthAnalyticsId;
    Long newUsersCount;
    Long activeUsersCount;
    LocalDateTime occurredAt;
    
    public static UserMetricsUpdatedEvent now(
        UUID userGrowthAnalyticsId,
        Long newUsersCount,
        Long activeUsersCount
    ) {
        return new UserMetricsUpdatedEvent(
            UUID.randomUUID(),
            userGrowthAnalyticsId,
            newUsersCount,
            activeUsersCount,
            LocalDateTime.now()
        );
    }
}

