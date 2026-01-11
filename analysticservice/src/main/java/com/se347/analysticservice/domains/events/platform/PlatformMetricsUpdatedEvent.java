package com.se347.analysticservice.domains.events.platform;

import lombok.Value;
import java.util.UUID;

import com.se347.analysticservice.domains.events.DomainEvent;

import java.time.LocalDateTime;

/**
 * Event published when platform metrics are updated.
 * This could trigger notifications or dashboards to refresh.
 */
@Value
public class PlatformMetricsUpdatedEvent implements DomainEvent {
    
    UUID eventId;
    UUID platformOverviewId;
    Long totalUsers;
    Long totalCourses;
    Long totalEnrollments;
    LocalDateTime occurredAt;
    
    public static PlatformMetricsUpdatedEvent now(
        UUID platformOverviewId,
        Long totalUsers,
        Long totalCourses,
        Long totalEnrollments
    ) {
        return new PlatformMetricsUpdatedEvent(
            UUID.randomUUID(),
            platformOverviewId,
            totalUsers,
            totalCourses,
            totalEnrollments,
            LocalDateTime.now()
        );
    }
}

