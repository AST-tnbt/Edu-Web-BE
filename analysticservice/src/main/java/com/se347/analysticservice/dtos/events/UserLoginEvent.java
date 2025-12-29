package com.se347.analysticservice.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO received when a user logs in (for active user tracking).
 * This is an external event from Auth Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginEvent {
    
    /**
     * Unique event identifier.
     */
    private UUID eventId;
    
    /**
     * ID of the user who logged in.
     */
    private UUID userId;
    
    /**
     * Login timestamp.
     */
    private LocalDateTime loginAt;
    
    /**
     * Event occurrence timestamp.
     */
    private LocalDateTime occurredAt;
}

