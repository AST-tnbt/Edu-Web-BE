package com.se347.userservice.services;

import com.se347.userservice.dtos.UserProfileRequestDto;
import com.se347.userservice.dtos.UserProfileResponseDto;
import com.se347.userservice.dtos.UserCreatedEventDto;
import java.util.UUID;

public interface UserProfileService {
    UserProfileResponseDto createProfile(UserProfileRequestDto request);
    UserProfileResponseDto createProfileDefault(UserCreatedEventDto userCreatedEvent);
    UserProfileResponseDto getProfileByEmail(String email);
    UserProfileResponseDto getProfileByUserId(UUID userId);
    UserProfileResponseDto updateProfile(UUID userId, UserProfileRequestDto request);
    boolean existsByUserId(UUID userId);
}
