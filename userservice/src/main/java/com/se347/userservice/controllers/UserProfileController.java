package com.se347.userservice.controllers;

import com.se347.userservice.dtos.UserProfileRequestDto;
import com.se347.userservice.dtos.UserProfileResponseDto;
import com.se347.userservice.services.UserProfileService;
import com.se347.userservice.exceptions.UserException;
import com.se347.userservice.exceptions.ValidationException;
import com.se347.userservice.exceptions.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/profiles")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);
    private final UserProfileService userProfileService;

    UserProfileController(UserProfileService userProfileService){
        this.userProfileService = userProfileService;
    }

    @PostMapping
    public ResponseEntity<UserProfileResponseDto> createProfile(@RequestBody UserProfileRequestDto request) {
        logger.info("Creating user profile for userId: {}", request.getUserId());
        try {
            UserProfileResponseDto profile = userProfileService.createProfile(request);
            logger.info("Successfully created user profile for userId: {}", request.getUserId());
            return ResponseEntity.ok(profile);
        } catch (UserException.UserProfileAlreadyExistsException e) {
            logger.warn("User profile already exists for userId: {}", request.getUserId());
            throw e; // Re-throw để GlobalExceptionHandler xử lý
        } catch (ValidationException e) {
            logger.warn("Validation error when creating profile: {}", e.getMessage());
            throw e; // Re-throw để GlobalExceptionHandler xử lý
        } catch (BusinessException e) {
            logger.error("Business error when creating profile: {}", e.getMessage());
            throw e; // Re-throw để GlobalExceptionHandler xử lý
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getCurrentProfile(
            @RequestHeader("X-User-Id") UUID userId,
            HttpServletRequest request
    ) {
        logger.info("X-User-Id header: {}", request.getHeader("X-User-Id"));
        logger.info("X-Username header: {}", request.getHeader("X-Username"));
        logger.info("X-User-Roles header: {}", request.getHeader("X-User-Roles"));
        logger.info("X-Authenticated header: {}", request.getHeader("X-Authenticated"));
        logger.info("Getting current user profile for userId: {}", userId);
        try {
            UserProfileResponseDto profile = userProfileService.getProfileByUserId(userId);
            logger.info("Successfully retrieved user profile for userId: {}", userId);
            return ResponseEntity.ok(profile);
        } catch (UserException.UserProfileNotFoundException e) {
            logger.warn("User profile not found for userId: {}", userId);
            throw e; // Re-throw để GlobalExceptionHandler xử lý
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDto> getProfile(
        @PathVariable UUID userId,
        @RequestHeader("X-User-Roles") String userRoles) {
        if (!userRoles.contains("ADMIN")) {
            throw new UserException.UserPermissionDeniedException("User not authorized to access this resource");
        }
        logger.info("Getting user profile for userId: {}", userId);
        try {
            UserProfileResponseDto profile = userProfileService.getProfileByUserId(userId);
            logger.info("Successfully retrieved user profile for userId: {}", userId);
            return ResponseEntity.ok(profile);
        } catch (UserException.UserProfileNotFoundException e) {
            logger.warn("User profile not found for userId: {}", userId);
            throw e; // Re-throw để GlobalExceptionHandler xử lý
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserProfileResponseDto> getProfileByEmail(@PathVariable String email) {
        logger.info("Getting user profile for email: {}", email);
        try {
            UserProfileResponseDto profile = userProfileService.getProfileByEmail(email);
            logger.info("Successfully retrieved user profile for email: {}", email);
            return ResponseEntity.ok(profile);
        }
        catch (UserException.UserProfileNotFoundException e) {
            logger.warn("User profile not found for email: {}", email);
            throw e; // Re-throw để GlobalExceptionHandler xử lý
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDto> updateProfile(
            @PathVariable UUID userId,
            @RequestBody UserProfileRequestDto request,
            @RequestHeader("X-User-Roles") String userRoles) {
        if (!userRoles.contains("ADMIN")) {
            throw new UserException.UserPermissionDeniedException("User not authorized to access this resource");
        }
        logger.info("Updating user profile for userId: {}", userId);
        try {
            UserProfileResponseDto profile = userProfileService.updateProfile(userId, request);
            logger.info("Successfully updated user profile for userId: {}", userId);
            return ResponseEntity.ok(profile);
        } catch (UserException.UserProfileNotFoundException e) {
            logger.warn("User profile not found for update, userId: {}", userId);
            throw e; // Re-throw để GlobalExceptionHandler xử lý
        } catch (ValidationException e) {
            logger.warn("Validation error when updating profile: {}", e.getMessage());
            throw e; // Re-throw để GlobalExceptionHandler xử lý
        } catch (BusinessException e) {
            logger.error("Business error when updating profile: {}", e.getMessage());
            throw e; // Re-throw để GlobalExceptionHandler xử lý
        }
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDto> updateCurrentProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody UserProfileRequestDto request) {
        logger.info("Updating current user profile for userId: {}", userId);
        try {
            UserProfileResponseDto profile = userProfileService.updateProfile(userId, request);
            logger.info("Successfully updated current user profile for userId: {}", userId);
            return ResponseEntity.ok(profile);
        }
        catch (UserException.UserProfileNotFoundException e) {
            logger.warn("User profile not found for update, userId: {}", userId);
            throw e; // Re-throw để GlobalExceptionHandler xử lý
        } catch (ValidationException e) {
            logger.warn("Validation error when updating profile: {}", e.getMessage());
            throw e; // Re-throw để GlobalExceptionHandler xử lý
        } catch (BusinessException e) {
            logger.error("Business error when updating profile: {}", e.getMessage());
            throw e; // Re-throw để GlobalExceptionHandler xử lý
        }
    }
}
