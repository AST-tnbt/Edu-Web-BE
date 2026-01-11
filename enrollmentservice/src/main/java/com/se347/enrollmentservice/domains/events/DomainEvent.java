package com.se347.enrollmentservice.domains.events;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {
    UUID getEventId();
    LocalDateTime getOccurredAt();
}
