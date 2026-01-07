package com.se347.analysticservice.domains.events.platform;

import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

import com.se347.analysticservice.domains.events.DomainEvent;

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

