package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.enrollmentservice.services.LearningProgressService;
import com.se347.enrollmentservice.domains.EnrollmentDomainService;
import com.se347.enrollmentservice.domains.LearningProgressDomainService;
import com.se347.enrollmentservice.dtos.LearningProgressRequestDto;
import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;
import com.se347.enrollmentservice.repositories.LearningProgressRepository;
import com.se347.enrollmentservice.entities.LearningProgress;
import com.se347.enrollmentservice.exceptions.LearningProgressException;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LearningProgressServiceImpl implements LearningProgressService {
    
    private final LearningProgressRepository learningProgressRepository;
    private final EnrollmentDomainService enrollmentDomainService;
    private final LearningProgressDomainService learningProgressDomainService;

    // ========== Public API ==========

    @Transactional
    @Override
    public LearningProgressResponseDto createLearningProgress(LearningProgressRequestDto request, UUID userId) {
        // Validate enrollment có thể truy cập (status, payment)
        enrollmentDomainService.ensureEnrollmentAccessible(request.getEnrollmentId());

        // Validate business rules through domain service
        learningProgressDomainService.validateLearningProgressCreation(request);

        // Validate user has access to enrollment
        if (!enrollmentDomainService.canUserAccessEnrollment(enrollmentDomainService.findEnrollmentById(request.getEnrollmentId()), userId)) {
            throw new LearningProgressException.UnauthorizedAccessException("User does not have access to this enrollment");
        }

        // Create entity through domain service
        LearningProgress learningProgress = learningProgressDomainService.createLearningProgressEntity(request);
        
        // Save through repository (infrastructure concern)
        learningProgressRepository.save(learningProgress);
        return mapToResponse(learningProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningProgressResponseDto getLearningProgressById(UUID learningProgressId, UUID userId) {
        LearningProgress learningProgress = learningProgressDomainService.findLearningProgressById(learningProgressId);
        if (!learningProgressDomainService.canUserAccessLearningProgress(learningProgress, userId)) {
            throw new LearningProgressException.UnauthorizedAccessException("User does not have access to this learning progress");
        }
        return mapToResponse(learningProgress);
    }

    @Transactional
    @Override
    public LearningProgressResponseDto getLearningProgressByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId, UUID userId) {
        // 1. Validate enrollment có thể truy cập (status, payment)
        enrollmentDomainService.ensureEnrollmentAccessible(enrollmentId);
        // 2. Get or create learning progress với retry logic để tránh race condition
        LearningProgress learningProgress = learningProgressDomainService.getOrCreateLearningProgress(lessonId, enrollmentId);
        
        // Validate user has access to learning progress
        if (!learningProgressDomainService.canUserAccessLearningProgress(learningProgress, userId)) {
            throw new LearningProgressException.UnauthorizedAccessException("User does not have access to this learning progress");
        }

        // 3. Update last accessed time through domain service
        learningProgressDomainService.updateLastAccessedEntity(learningProgress);
        learningProgressRepository.save(learningProgress);
        
        return mapToResponse(learningProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningProgressResponseDto> getLearningProgressByEnrollmentId(UUID enrollmentId, UUID userId) {
        // Validate enrollment exists
        if (!enrollmentDomainService.enrollmentExists(enrollmentId)) {
            throw new LearningProgressException.EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId);
        }
        // Validate user has access to enrollment
        if (!enrollmentDomainService.canUserAccessEnrollment(enrollmentDomainService.findEnrollmentById(enrollmentId), userId)) {
            throw new LearningProgressException.UnauthorizedAccessException("User does not have access to this enrollment");
        }

        List<LearningProgress> learningProgresses = learningProgressDomainService.findLearningProgressByEnrollmentId(enrollmentId);
        return learningProgresses.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public LearningProgressResponseDto updateLearningProgress(UUID learningProgressId, LearningProgressRequestDto request, UUID userId) {
        if (learningProgressId == null) {
            throw new LearningProgressException.InvalidRequestException("Learning progress ID cannot be null");
        }
        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }

        // Get learning progress through domain service
        LearningProgress learningProgress = learningProgressDomainService.findLearningProgressById(learningProgressId);
        if (!learningProgressDomainService.canUserAccessLearningProgress(learningProgress, userId)) {
            throw new LearningProgressException.UnauthorizedAccessException("User does not have access to this learning progress");
        }
        // Store original completion status for sync
        boolean originalCompleted = learningProgress.isCompleted();

        // Validate business rules through domain service
        learningProgressDomainService.validateLearningProgressUpdate(learningProgress, request);

        // Update entity through domain service
        learningProgressDomainService.updateLearningProgressEntity(learningProgress, request);
        
        // Save through repository (infrastructure concern)
        learningProgressRepository.save(learningProgress);
        
        // Sync with CourseProgress if completion status changed (orchestration)
        syncCourseProgressOnCompletionChange(learningProgress, originalCompleted);

        return mapToResponse(learningProgress);
    }

    // PATCH: Update only provided fields
    @Transactional
    @Override
    public LearningProgressResponseDto patchLearningProgress(UUID learningProgressId, LearningProgressRequestDto request, UUID userId) {
        if (learningProgressId == null) {
            throw new LearningProgressException.InvalidRequestException("Learning progress ID cannot be null");
        }
        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }

        // Get learning progress through domain service
        LearningProgress learningProgress = learningProgressDomainService.findLearningProgressById(learningProgressId);
        if (!learningProgressDomainService.canUserAccessLearningProgress(learningProgress, userId)) {
            throw new LearningProgressException.UnauthorizedAccessException("User does not have access to this learning progress");
        }
        // Store original completion status for sync
        boolean originalCompleted = learningProgress.isCompleted();

        // Patch entity through domain service
        learningProgressDomainService.patchLearningProgressEntity(learningProgress, request);
        
        // Save through repository (infrastructure concern)
        learningProgressRepository.save(learningProgress);
        
        // Sync with CourseProgress if completion status changed (orchestration)
        syncCourseProgressOnCompletionChange(learningProgress, originalCompleted);

        return mapToResponse(learningProgress);
    }

    // Mark as completed by lesson and enrollment
    @Transactional
    @Override
    public LearningProgressResponseDto markAsCompleted(UUID lessonId, UUID enrollmentId) {
        // Get learning progress through domain service
        LearningProgress learningProgress = learningProgressDomainService.findLearningProgressByLessonIdAndEnrollmentId(lessonId, enrollmentId);

        boolean originalCompleted = learningProgress.isCompleted();
        
        // Mark as completed through domain service
        learningProgressDomainService.markAsCompletedEntity(learningProgress);
        
        // Save through repository (infrastructure concern)
        learningProgressRepository.save(learningProgress);
        
        // Sync with CourseProgress if completion status changed (orchestration)
        syncCourseProgressOnCompletionChange(learningProgress, originalCompleted);

        return mapToResponse(learningProgress);
    }

    // Update last accessed time
    @Transactional
    @Override
    public LearningProgressResponseDto updateLastAccessed(UUID learningProgressId) {
        // Get learning progress through domain service
        LearningProgress learningProgress = learningProgressDomainService.findLearningProgressById(learningProgressId);

        // Update last accessed through domain service
        learningProgressDomainService.updateLastAccessedEntity(learningProgress);
        
        // Save through repository (infrastructure concern)
        learningProgressRepository.save(learningProgress);

        return mapToResponse(learningProgress);
    }

    // ========== Mapping ==========

    private LearningProgressResponseDto mapToResponse(LearningProgress learningProgress) {
        return LearningProgressResponseDto.builder()
            .learningProgressId(learningProgress.getLearningProgressId())
            .enrollmentId(learningProgress.getEnrollmentId())
            .lessonId(learningProgress.getLessonId())
            .isCompleted(learningProgress.isCompleted())
            .lastAccessedAt(learningProgress.getLastAccessedAt())
            .completedAt(learningProgress.getCompletedAt())
            .build();
    }

    // ========== Orchestration ==========
    
    /**
     * Syncs CourseProgress when a lesson completion status changes
     * - If lesson just completed: increment lessonsCompleted
     * - If lesson uncompleted: decrement lessonsCompleted
     */
    private void syncCourseProgressOnCompletionChange(LearningProgress learningProgress, boolean originalCompleted) {
        boolean currentCompleted = learningProgress.isCompleted();

        if (currentCompleted == originalCompleted) {
            return;
        }

        boolean increment = currentCompleted && !originalCompleted;
        enrollmentDomainService.syncCourseProgressOnLessonChange(
            learningProgress.getEnrollmentId(),
            increment
        );
    }
}