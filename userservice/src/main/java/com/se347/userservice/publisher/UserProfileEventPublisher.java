package com.se347.userservice.publisher;

import com.se347.userservice.entities.UserProfile;

public interface UserProfileEventPublisher {
    void publishUserProfileCompletedEvent(UserProfile userProfile);
}
