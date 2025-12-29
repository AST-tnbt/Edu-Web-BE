package com.se347.analysticservice.entities.events;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {
    UUID getEventId();
    LocalDateTime getOccurredAt();
}

