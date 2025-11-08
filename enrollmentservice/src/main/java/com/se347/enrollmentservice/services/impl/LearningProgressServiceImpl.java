package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;
import com.se347.enrollmentservice.services.LearningProgressService;
import com.se347.enrollmentservice.dtos.LearningProgressRequestDto;
import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;
import com.se347.enrollmentservice.repositories.LearningProgressRepository;
import com.se347.enrollmentservice.entities.LearningProgress;
import com.se347.enrollmentservice.exceptions.LearningProgressException;
import com.se347.enrollmentservice.services.EnrollmentService;
import com.se347.enrollmentservice.services.CourseProgressService;
import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LearningProgressServiceImpl implements LearningProgressService {

    private final LearningProgressRepository learningProgressRepository;
    private final EnrollmentService enrollmentService;
    private final CourseProgressService courseProgressService;

    // ========== Public API ==========

    @Transactional
    @Override
    public LearningProgressResponseDto createLearningProgress(LearningProgressRequestDto request) {
        validateCreateRequest(request);
        
        // Verify enrollment exists
        if (!enrollmentService.isEnrollmentExists(request.getEnrollmentId())) {
            throw new LearningProgressException.InvalidRequestException(
                "Enrollment not found with ID: " + request.getEnrollmentId());
        }

        // Check for duplicate learning progress
        LearningProgress existing = learningProgressRepository.findByLessonIdAndEnrollmentId(
            request.getLessonId(), request.getEnrollmentId());
        if (existing != null) {
            throw new LearningProgressException.DuplicateLearningProgressException(
                "Learning progress already exists for lesson " + request.getLessonId() + 
                " and enrollment " + request.getEnrollmentId());
        }

        LearningProgress learningProgress = LearningProgress.builder()
            .enrollmentId(request.getEnrollmentId())
            .lessonId(request.getLessonId())
            .isCompleted(false)
            .lastAccessedAt(LocalDateTime.now())
            .build();

        learningProgressRepository.save(learningProgress);
        return mapToResponse(learningProgress);
    }

    @Override
    public LearningProgressResponseDto getLearningProgressById(UUID learningProgressId) {
        LearningProgress learningProgress = learningProgressRepository.findById(learningProgressId)
            .orElseThrow(() -> new LearningProgressException.LearningProgressNotFoundException(
                "Learning progress not found with ID: " + learningProgressId));
        return mapToResponse(learningProgress);
    }

    @Transactional
    @Override
    public LearningProgressResponseDto getLearningProgressByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId) {
        LearningProgress learningProgress = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
        if (learningProgress == null) {
            createLearningProgress(LearningProgressRequestDto.builder()
                .lessonId(lessonId)
                .enrollmentId(enrollmentId)
                .build());
            learningProgress = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
        }
        return mapToResponse(learningProgress);
    }

    @Override
    public List<LearningProgressResponseDto> getLearningProgressByEnrollmentId(UUID enrollmentId) {
        return learningProgressRepository.findByEnrollmentId(enrollmentId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public LearningProgressResponseDto updateLearningProgress(UUID learningProgressId, LearningProgressRequestDto request) {
        validateUpdateRequest(learningProgressId, request);

        LearningProgress learningProgress = learningProgressRepository.findById(learningProgressId)
            .orElseThrow(() -> new LearningProgressException.LearningProgressNotFoundException(
                "Learning progress not found with ID: " + learningProgressId));

        // Store original completion status for validation
        boolean originalCompleted = learningProgress.isCompleted();

        // Apply updates
        learningProgress.setCompleted(request.isCompleted());
        learningProgress.setLastAccessedAt(LocalDateTime.now());

        // Handle completion status change
        updateCompletionStatus(learningProgress, originalCompleted);
        
        learningProgressRepository.save(learningProgress);
        
        // Sync with CourseProgress if completion status changed
        syncCourseProgressOnCompletionChange(learningProgress, originalCompleted);

        return mapToResponse(learningProgress);
    }

    // PATCH: Update only provided fields
    @Transactional
    @Override
    public LearningProgressResponseDto patchLearningProgress(UUID learningProgressId, LearningProgressRequestDto request) {
        validatePatchRequest(learningProgressId, request);

        LearningProgress learningProgress = learningProgressRepository.findById(learningProgressId)
            .orElseThrow(() -> new LearningProgressException.LearningProgressNotFoundException(
                "Learning progress not found with ID: " + learningProgressId));

        boolean originalCompleted = learningProgress.isCompleted();

        // Update completed status if provided in request
        // Note: boolean default is false, so we need to check if it was explicitly set
        // For PATCH, we'll update if request.isCompleted() is true (to mark as completed)
        // or if we want to uncomplete, we need a different approach
        // For now, we'll update if the value is different from current
        if (request.isCompleted() != learningProgress.isCompleted()) {
            learningProgress.setCompleted(request.isCompleted());
        }

        // Always update last accessed time
        learningProgress.setLastAccessedAt(LocalDateTime.now());

        // Handle completion status change
        updateCompletionStatus(learningProgress, originalCompleted);
        
        learningProgressRepository.save(learningProgress);
        
        // Sync with CourseProgress if completion status changed
        syncCourseProgressOnCompletionChange(learningProgress, originalCompleted);

        return mapToResponse(learningProgress);
    }

    // Mark as completed by lesson and enrollment
    @Transactional
    @Override
    public LearningProgressResponseDto markAsCompleted(UUID lessonId, UUID enrollmentId) {
        LearningProgress learningProgress = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
        if (learningProgress == null) {
            throw new LearningProgressException.LearningProgressNotFoundException(
                "Learning progress not found for lesson " + lessonId + " and enrollment " + enrollmentId);
        }

        boolean originalCompleted = learningProgress.isCompleted();
        
        if (!learningProgress.isCompleted()) {
            learningProgress.setCompleted(true);
            learningProgress.setCompletedAt(LocalDateTime.now());
            learningProgress.setLastAccessedAt(LocalDateTime.now());
            learningProgressRepository.save(learningProgress);
            
            // Sync with CourseProgress
            syncCourseProgressOnCompletionChange(learningProgress, originalCompleted);
        }

        return mapToResponse(learningProgress);
    }

    // Update last accessed time
    @Transactional
    @Override
    public LearningProgressResponseDto updateLastAccessed(UUID learningProgressId) {
        LearningProgress learningProgress = learningProgressRepository.findById(learningProgressId)
            .orElseThrow(() -> new LearningProgressException.LearningProgressNotFoundException(
                "Learning progress not found with ID: " + learningProgressId));

        learningProgress.setLastAccessedAt(LocalDateTime.now());
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

    // ========== Validation ==========

    private void validateCreateRequest(LearningProgressRequestDto request) {
        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }
        if (request.getEnrollmentId() == null) {
            throw new LearningProgressException.InvalidRequestException("Enrollment ID cannot be null");
        }
        if (request.getLessonId() == null) {
            throw new LearningProgressException.InvalidRequestException("Lesson ID cannot be null");
        }
    }

    private void validateUpdateRequest(UUID learningProgressId, LearningProgressRequestDto request) {
        if (learningProgressId == null) {
            throw new LearningProgressException.InvalidRequestException("Learning progress ID cannot be null");
        }
        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }
    }

    private void validatePatchRequest(UUID learningProgressId, LearningProgressRequestDto request) {
        if (learningProgressId == null) {
            throw new LearningProgressException.InvalidRequestException("Learning progress ID cannot be null");
        }
        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }
    }

    // ========== Business Logic ==========

    private void updateCompletionStatus(LearningProgress learningProgress, boolean originalCompleted) {
        boolean currentCompleted = learningProgress.isCompleted();
        
        if (currentCompleted && !originalCompleted) {
            // Just completed
            learningProgress.setCompletedAt(LocalDateTime.now());
        } else if (!currentCompleted && originalCompleted) {
            // Uncompleted (rare case, but handle it)
            learningProgress.setCompletedAt(null);
        }
        // If status didn't change, leave completedAt as is
    }
    
    /**
     * Syncs CourseProgress when a lesson completion status changes
     * - If lesson just completed: increment lessonsCompleted
     * - If lesson uncompleted: decrement lessonsCompleted
     */
    private void syncCourseProgressOnCompletionChange(LearningProgress learningProgress, boolean originalCompleted) {
        boolean currentCompleted = learningProgress.isCompleted();
        
        // Only sync if completion status actually changed
        if (currentCompleted == originalCompleted) {
            return;
        }
        
        try {
            // Get CourseProgress by enrollmentId
            CourseProgressResponseDto courseProgress = courseProgressService.getCourseProgressByEnrollmentId(learningProgress.getEnrollmentId());
            
            // Calculate new lessonsCompleted count
            int currentLessonsCompleted = courseProgress.getLessonsCompleted();
            int newLessonsCompleted;
            
            if (currentCompleted && !originalCompleted) {
                // Lesson just completed - increment
                newLessonsCompleted = currentLessonsCompleted + 1;
            } else {
                // Lesson uncompleted - decrement (but not below 0)
                newLessonsCompleted = Math.max(0, currentLessonsCompleted - 1);
            }
            
            // Update CourseProgress using patchCourseProgress
            courseProgressService.patchCourseProgress(
                courseProgress.getCourseProgressId(),
                CourseProgressRequestDto.builder()
                    .lessonsCompleted(newLessonsCompleted)
                    .build()
            );
        } catch (Exception e) {
            // Log error but don't fail the LearningProgress update
            // This ensures LearningProgress is saved even if CourseProgress update fails
            // In production, you might want to use a more sophisticated error handling strategy
            throw new LearningProgressException.InvalidRequestException(
                "Failed to sync CourseProgress: " + e.getMessage());
        }
    }
}