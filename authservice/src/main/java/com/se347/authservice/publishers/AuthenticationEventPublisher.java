package com.se347.authservice.publishers;

import com.se347.authservice.entities.User;

public interface AuthenticationEventPublisher {
    public void publishUserCreatedEvent(User user);
}
