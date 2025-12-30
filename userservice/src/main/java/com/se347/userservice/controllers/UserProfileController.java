package com.se347.userservice.controllers;

import com.se347.userservice.dtos.UserProfileRequestDto;
import com.se347.userservice.dtos.UserProfileResponseDto;
import com.se347.userservice.services.UserProfileService;
import com.se347.userservice.exceptions.UserException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import java.util.UUID;


@RestController
@RequestMapping("/api/users/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping
    public ResponseEntity<UserProfileResponseDto> createProfile(@RequestBody UserProfileRequestDto request) {
        UserProfileResponseDto profile = userProfileService.createProfile(request);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getCurrentProfile(
            @RequestHeader("X-User-Id") UUID userId,
            HttpServletRequest request
    ) {
        UserProfileResponseDto profile = userProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/id/{userId}")
    public ResponseEntity<UserProfileResponseDto> getProfile(
        @PathVariable UUID userId,
        @RequestHeader("X-User-Roles") String userRoles) {
        UserProfileResponseDto profile = userProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/slug/{userSlug}")
    public ResponseEntity<UserProfileResponseDto> getProfileByUserSlug(@PathVariable String userSlug) {
        UserProfileResponseDto profile = userProfileService.getProfileByUserSlug(userSlug);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserProfileResponseDto> getProfileByEmail(@PathVariable String email) {
        UserProfileResponseDto profile = userProfileService.getProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/id/{userId}")
    public ResponseEntity<UserProfileResponseDto> updateProfile(
            @PathVariable UUID userId,
            @RequestBody UserProfileRequestDto request,
            @RequestHeader("X-User-Roles") String userRoles) {
        boolean isAdmin = false;
        for (String role : userRoles.split(",")) {
            if ("ADMIN".equals(role.trim())) {
                isAdmin = true;
                break;
            }
        }
        if (!isAdmin) {
            throw new UserException.UnauthorizedAccessException("User not authorized to access this resource");
        }
        UserProfileResponseDto profile = userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDto> updateCurrentProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody UserProfileRequestDto request) {
        UserProfileResponseDto profile = userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(profile);
    }
}
