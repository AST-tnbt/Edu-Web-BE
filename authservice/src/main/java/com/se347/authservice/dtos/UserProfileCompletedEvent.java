package com.se347.authservice.dtos;

import java.io.Serializable;
import java.util.UUID;

public class UserProfileCompletedEvent implements Serializable {
    private UUID userId;

    public UserProfileCompletedEvent() {}

    public UserProfileCompletedEvent(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}

