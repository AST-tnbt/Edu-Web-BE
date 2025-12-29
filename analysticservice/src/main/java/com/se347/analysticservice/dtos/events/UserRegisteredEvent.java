package com.se347.analysticservice.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO received from Auth/User Service when a user registers.
 * This is an external event from another bounded context.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisteredEvent {
    
    /**
     * Unique event identifier.
     */
    private UUID eventId;
    
    /**
     * ID of the newly registered user.
     */
    private UUID userId;
    
    /**
     * Email of the user (for analytics purposes).
     */
    private String email;
    
    /**
     * Username of the user.
     */
    private String username;
    
    /**
     * User role (STUDENT, INSTRUCTOR, ADMIN).
     * For analytics, we track only non-admin users.
     */
    private String role;
    
    /**
     * Timestamp when the user registered.
     */
    private LocalDateTime registeredAt;
    
    /**
     * Event occurrence timestamp.
     */
    private LocalDateTime occurredAt;
}

