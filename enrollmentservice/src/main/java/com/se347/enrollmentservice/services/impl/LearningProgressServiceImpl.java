package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;
import com.se347.enrollmentservice.services.LearningProgressService;
import com.se347.enrollmentservice.dtos.LearningProgressRequestDto;
import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;
import com.se347.enrollmentservice.repositories.LearningProgressRepository;
import com.se347.enrollmentservice.entities.LearningProgress;
import com.se347.enrollmentservice.exceptions.LearningProgressException;
import com.se347.enrollmentservice.services.EnrollmentService;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class LearningProgressServiceImpl implements LearningProgressService {

    private final LearningProgressRepository learningProgressRepository;
    private final EnrollmentService enrollmentService;

    public LearningProgressServiceImpl(LearningProgressRepository learningProgressRepository,
                                     EnrollmentService enrollmentService) {
        this.learningProgressRepository = learningProgressRepository;
        this.enrollmentService = enrollmentService;
    }

    // ========== Public API ==========

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
            .contentId(request.getContentId())
            .isCompleted(request.isCompleted())
            .lastAccessedAt(LocalDateTime.now())
            .completedAt(request.isCompleted() ? LocalDateTime.now() : null)
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

    @Override
    public LearningProgressResponseDto getLearningProgressByContentIdAndEnrollmentId(UUID contentId, UUID enrollmentId) {
        LearningProgress learningProgress = learningProgressRepository.findByContentIdAndEnrollmentId(contentId, enrollmentId);

        if (learningProgress == null) {
            throw new LearningProgressException.LearningProgressNotFoundException(
            "Learning progress not found for content " + contentId + " and enrollment " + enrollmentId);
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

    @Override
    public LearningProgressResponseDto updateLearningProgress(UUID learningProgressId, LearningProgressRequestDto request) {
        validateUpdateRequest(learningProgressId, request);

        LearningProgress learningProgress = learningProgressRepository.findById(learningProgressId)
            .orElseThrow(() -> new LearningProgressException.LearningProgressNotFoundException(
                "Learning progress not found with ID: " + learningProgressId));

        // Store original completion status for validation
        boolean originalCompleted = learningProgress.isCompleted();

        // Apply updates
        learningProgress.setContentId(request.getContentId());
        learningProgress.setCompleted(request.isCompleted());
        learningProgress.setLastAccessedAt(LocalDateTime.now());

        // Handle completion status change
        updateCompletionStatus(learningProgress, originalCompleted);

        return mapToResponse(learningProgressRepository.save(learningProgress));
    }

    // PATCH: Update only provided fields
    @Override
    public LearningProgressResponseDto patchLearningProgress(UUID learningProgressId, LearningProgressRequestDto request) {
        validatePatchRequest(learningProgressId, request);

        LearningProgress learningProgress = learningProgressRepository.findById(learningProgressId)
            .orElseThrow(() -> new LearningProgressException.LearningProgressNotFoundException(
                "Learning progress not found with ID: " + learningProgressId));

        boolean originalCompleted = learningProgress.isCompleted();

        // Apply partial updates
        if (request.getContentId() != null) {
            learningProgress.setContentId(request.getContentId());
        }
        if (!request.isCompleted()) {
            learningProgress.setCompleted(request.isCompleted());
        }

        // Always update last accessed time
        learningProgress.setLastAccessedAt(LocalDateTime.now());

        // Handle completion status change
        updateCompletionStatus(learningProgress, originalCompleted);

        return mapToResponse(learningProgressRepository.save(learningProgress));
    }

    // Mark as completed by lesson and enrollment
    @Override
    public LearningProgressResponseDto markAsCompleted(UUID lessonId, UUID enrollmentId) {
        LearningProgress learningProgress = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
        if (learningProgress == null) {
            throw new LearningProgressException.LearningProgressNotFoundException(
                "Learning progress not found for lesson " + lessonId + " and enrollment " + enrollmentId);
        }

        if (!learningProgress.isCompleted()) {
            learningProgress.setCompleted(true);
            learningProgress.setCompletedAt(LocalDateTime.now());
            learningProgress.setLastAccessedAt(LocalDateTime.now());
            learningProgressRepository.save(learningProgress);
        }

        return mapToResponse(learningProgress);
    }

    // Update last accessed time
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
            .contentId(learningProgress.getContentId())
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
        if (request.getContentId() == null) {
            throw new LearningProgressException.InvalidRequestException("Content ID cannot be null");
        }
    }

    private void validateUpdateRequest(UUID learningProgressId, LearningProgressRequestDto request) {
        if (learningProgressId == null) {
            throw new LearningProgressException.InvalidRequestException("Learning progress ID cannot be null");
        }
        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }
        if (request.getContentId() == null) {
            throw new LearningProgressException.InvalidRequestException("Content ID cannot be null");
        }
    }

    private void validatePatchRequest(UUID learningProgressId, LearningProgressRequestDto request) {
        if (learningProgressId == null) {
            throw new LearningProgressException.InvalidRequestException("Learning progress ID cannot be null");
        }
        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }

        boolean hasAny = request.getContentId() != null;
        if (!hasAny) {
            throw new LearningProgressException.InvalidRequestException("At least one field must be provided for update");
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
}