package com.se347.analysticservice.entities.events.platform;

import com.se347.analysticservice.entities.events.DomainEvent;
import com.se347.analysticservice.enums.Period;
import lombok.Value;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a new PlatformOverview snapshot is created.
 * This indicates a new period (daily/weekly/monthly) overview has been generated.
 */
@Value
public class PlatformOverviewCreatedEvent implements DomainEvent {
    
    UUID eventId;
    UUID platformOverviewId;
    Period period;
    LocalDate startDate;
    LocalDate endDate;
    LocalDateTime occurredAt;
    
    public static PlatformOverviewCreatedEvent now(
        UUID platformOverviewId,
        Period period,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return new PlatformOverviewCreatedEvent(
            UUID.randomUUID(),
            platformOverviewId,
            period,
            startDate,
            endDate,
            LocalDateTime.now()
        );
    }
}

