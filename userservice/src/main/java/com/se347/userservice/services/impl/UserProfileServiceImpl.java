package com.se347.userservice.services.impl;

import com.se347.userservice.dtos.UserProfileRequestDto;
import com.se347.userservice.dtos.UserProfileResponseDto;
import com.se347.userservice.entities.UserProfile;
import com.se347.userservice.repositories.UserProfileRepository;
import com.se347.userservice.exceptions.ResourceNotFoundException;
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
        System.out.println("Request userId: " + request.getUserId());
        
        UserProfile profile = UserProfile.builder()
                .userId(request.getUserId())
                .fullName(request.getFullName())
                .avatarUrl(request.getAvatarUrl())
                .bio(request.getBio())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .build();

        System.out.println("Profile userId before save: " + profile.getUserId());
        
        profile.onCreate();
        userProfileRepository.save(profile);
        
        System.out.println("Profile userId after save: " + profile.getUserId());
        
        return mapToResponse(profile);
    }

    // public UserProfileResponseDto getProfileByEmail(String email) {
    //     UserProfile profile = userProfileRepository.findByEmail(email)
    //             .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

    //     return mapToResponse(profile);
    // }

    @Override
    public UserProfileResponseDto getProfileByUserId(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for userId: " + userId));
        return mapToResponse(profile);
    }

    @Override
    public UserProfileResponseDto updateProfile(UUID userId, UserProfileRequestDto request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for userId: " + userId));

        profile.setFullName(request.getFullName());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setBio(request.getBio());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAddress(request.getAddress());
        profile.onUpdate();
        userProfileRepository.save(profile);
        return mapToResponse(profile);
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
