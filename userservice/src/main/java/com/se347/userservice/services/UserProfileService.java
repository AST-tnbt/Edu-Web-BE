package com.se347.userservice.services;

import com.se347.userservice.dtos.UserProfileRequestDto;
import com.se347.userservice.dtos.UserProfileResponseDto;
import java.util.UUID;

public interface UserProfileService {
    UserProfileResponseDto createProfile(UserProfileRequestDto request);
    // UserProfileResponseDto getProfileByEmail(String email);
    UserProfileResponseDto getProfileByUserId(UUID userId);
    UserProfileResponseDto updateProfile(UUID userId, UserProfileRequestDto request);
}
