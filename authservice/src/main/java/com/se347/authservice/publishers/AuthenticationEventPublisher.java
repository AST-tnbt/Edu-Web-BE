package com.se347.authservice.publishers;

import com.se347.authservice.entities.User;
import java.time.LocalDateTime;
import java.util.UUID;

public interface AuthenticationEventPublisher {
    public void publishUserCreatedEvent(User user);
    public void publishUserLoginEvent(UUID userId, LocalDateTime loginAt);
}
