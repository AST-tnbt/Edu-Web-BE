package com.se347.analysticservice.dtos.events.user;

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

    private UUID eventId;
    
    private UUID userId;
    
    private String email;

    private LocalDateTime createdAt;
    
    private LocalDateTime occurredAt;
}

