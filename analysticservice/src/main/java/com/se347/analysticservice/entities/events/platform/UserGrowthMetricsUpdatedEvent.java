package com.se347.analysticservice.entities.events.platform;

import com.se347.analysticservice.entities.events.DomainEvent;
import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when user growth metrics are updated.
 */
@Value
public class UserGrowthMetricsUpdatedEvent implements DomainEvent {
    
    UUID eventId;
    UUID userGrowthAnalyticsId;
    Long newUsersCount;
    Long activeUsersCount;
    Long totalUsers;
    LocalDateTime occurredAt;
    
    public static UserGrowthMetricsUpdatedEvent now(
        UUID userGrowthAnalyticsId,
        Long newUsersCount,
        Long activeUsersCount,
        Long totalUsers
    ) {
        return new UserGrowthMetricsUpdatedEvent(
            UUID.randomUUID(),
            userGrowthAnalyticsId,
            newUsersCount,
            activeUsersCount,
            totalUsers,
            LocalDateTime.now()
        );
    }
}

