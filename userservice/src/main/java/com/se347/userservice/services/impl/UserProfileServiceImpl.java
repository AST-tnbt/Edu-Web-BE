package com.se347.userservice.services.impl;

import com.se347.userservice.dtos.UserProfileRequestDto;
import com.se347.userservice.dtos.UserProfileResponseDto;
import com.se347.userservice.entities.UserProfile;
import com.se347.userservice.repositories.UserProfileRepository;
import com.se347.userservice.exceptions.UserException;
import com.se347.userservice.exceptions.BusinessException;
import com.se347.userservice.exceptions.ExceptionUtils;
import com.se347.userservice.services.UserProfileService;
import com.se347.userservice.dtos.UserCreatedEventDto;
import com.se347.userservice.publisher.UserProfileEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileEventPublisher userProfileEventPublisher;

    @Override
    @Transactional
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
                .email(request.getEmail())
                .avatarUrl(request.getAvatarUrl())
                .bio(request.getBio())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .profileCompleted(false)
                .build();

        try {
            profile.onCreate();
            userProfileRepository.save(profile);
            return mapToResponse(profile);
        } catch (DataAccessException e) {
            log.error("Failed to save user profile. userId={}", request.getUserId(), e);
            throw new BusinessException.DependencyException("Database", "Failed to save user profile", e);
        }
    }

    @Transactional
    @Override
    public UserProfileResponseDto createProfileDefault(UserCreatedEventDto userCreatedEvent) {
        // Idempotency: safeguard against duplicate events
        return userProfileRepository.findByUserId(userCreatedEvent.getUserId())
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    UserProfile profile = UserProfile.builder()
                            .userId(userCreatedEvent.getUserId())
                            .email(userCreatedEvent.getEmail())
                            .profileCompleted(false)
                            .build();
                    profile.onCreate();
                    try {
                        userProfileRepository.save(profile);
                        return mapToResponse(profile);
                    } catch (DataAccessException e) {
                        log.error("Failed to save default user profile. userId={}", userCreatedEvent.getUserId(), e);
                        throw new BusinessException.DependencyException("Database", "Failed to save user profile", e);
                    }
                });
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDto getProfileByEmail(String email) {
        // Validate input
        ExceptionUtils.validateNotNull(email, "email");
        
        UserProfile profile = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new UserException.UserProfileNotFoundException(email));

        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDto getProfileByUserId(UUID userId) {
        // Validate input
        ExceptionUtils.validateNotNull(userId, "userId");
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserException.UserProfileNotFoundException(userId.toString()));
        
        return mapToResponse(profile);
    }

    @Override
    @Transactional
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
        profile.setProfileCompleted(true);
        profile.onUpdate();

        try {
            userProfileRepository.save(profile);

            // Publish user profile completed event
            userProfileEventPublisher.publishUserProfileCompletedEvent(profile);

            return mapToResponse(profile);
        } catch (DataAccessException e) {
            log.error("Failed to update user profile. userId={}", userId, e);
            throw new BusinessException.DependencyException("Database", "Failed to update user profile", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        return userProfileRepository.existsByUserId(userId);
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
                .email(profile.getEmail())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .phoneNumber(profile.getPhoneNumber())
                .address(profile.getAddress())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
