package com.se347.analysticservice.entities.events.platform;

import com.se347.analysticservice.entities.events.DomainEvent;
import lombok.Value;
import java.util.UUID;
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

