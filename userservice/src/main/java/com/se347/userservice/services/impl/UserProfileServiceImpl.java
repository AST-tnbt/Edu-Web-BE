package com.se347.userservice.services.impl;

import com.se347.userservice.dtos.UserProfileRequestDto;
import com.se347.userservice.dtos.UserProfileResponseDto;
import com.se347.userservice.entities.UserProfile;
import com.se347.userservice.repositories.UserProfileRepository;
import com.se347.userservice.exceptions.UserException;
import com.se347.userservice.exceptions.BusinessException;
import com.se347.userservice.exceptions.ExceptionUtils;
import com.se347.userservice.services.UserProfileService;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    UserProfileServiceImpl(UserProfileRepository userProfileRepository){
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public UserProfileResponseDto createProfile(UserProfileRequestDto request) {
        // Validate input
        validateCreateProfileRequest(request);
        
        // Check if profile already exists
        if (userProfileRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new UserException.UserProfileAlreadyExistsException(request.getUserId().toString());
        }
        
        UserProfile profile = UserProfile.builder()
                .userId(request.getUserId())
                .fullName(request.getFullName())
                .avatarUrl(request.getAvatarUrl())
                .bio(request.getBio())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .build();

        try {
            profile.onCreate();
            userProfileRepository.save(profile);
            return mapToResponse(profile);
        } catch (Exception e) {
            throw new BusinessException.DependencyException("Database", "Failed to save user profile", e);
        }
    }

    // public UserProfileResponseDto getProfileByEmail(String email) {
    //     UserProfile profile = userProfileRepository.findByEmail(email)
    //             .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

    //     return mapToResponse(profile);
    // }

    @Override
    public UserProfileResponseDto getProfileByUserId(UUID userId) {
        // Validate input
        ExceptionUtils.validateNotNull(userId, "userId");
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserException.UserProfileNotFoundException(userId.toString()));
        
        return mapToResponse(profile);
    }

    @Override
    public UserProfileResponseDto updateProfile(UUID userId, UserProfileRequestDto request) {
        // Validate input
        ExceptionUtils.validateNotNull(userId, "userId");
        validateUpdateProfileRequest(request);
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserException.UserProfileNotFoundException(userId.toString()));

        // Update fields
        profile.setFullName(request.getFullName());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setBio(request.getBio());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAddress(request.getAddress());
        profile.onUpdate();
        
        try {
            userProfileRepository.save(profile);
            return mapToResponse(profile);
        } catch (Exception e) {
            throw new BusinessException.DependencyException("Database", "Failed to update user profile", e);
        }
    }

    /**
     * Validate create profile request
     */
    private void validateCreateProfileRequest(UserProfileRequestDto request) {
        ExceptionUtils.validateNotNull(request, "request");
        ExceptionUtils.validateNotNull(request.getUserId(), "userId");
        ExceptionUtils.validateRequired(request.getFullName(), "fullName");
        ExceptionUtils.validateStringLength(request.getFullName(), "fullName", 2, 100);
        
        if (request.getPhoneNumber() != null) {
            ExceptionUtils.validatePhoneNumber(request.getPhoneNumber(), "phoneNumber");
        }
        
        if (request.getBio() != null) {
            ExceptionUtils.validateStringLength(request.getBio(), "bio", 0, 500);
        }
        
        if (request.getAddress() != null) {
            ExceptionUtils.validateStringLength(request.getAddress(), "address", 0, 200);
        }
    }
    
    /**
     * Validate update profile request
     */
    private void validateUpdateProfileRequest(UserProfileRequestDto request) {
        ExceptionUtils.validateNotNull(request, "request");
        
        if (request.getFullName() != null) {
            ExceptionUtils.validateStringLength(request.getFullName(), "fullName", 2, 100);
        }
        
        if (request.getPhoneNumber() != null) {
            ExceptionUtils.validatePhoneNumber(request.getPhoneNumber(), "phoneNumber");
        }
        
        if (request.getBio() != null) {
            ExceptionUtils.validateStringLength(request.getBio(), "bio", 0, 500);
        }
        
        if (request.getAddress() != null) {
            ExceptionUtils.validateStringLength(request.getAddress(), "address", 0, 200);
        }
    }

    private UserProfileResponseDto mapToResponse(UserProfile profile) {
        return UserProfileResponseDto.builder()
                .userId(profile.getUserId())
                .fullName(profile.getFullName())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .phoneNumber(profile.getPhoneNumber())
                .address(profile.getAddress())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
