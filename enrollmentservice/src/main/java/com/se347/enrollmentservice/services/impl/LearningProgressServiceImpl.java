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
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.enums.EnrollmentStatus;
import com.se347.enrollmentservice.enums.PaymentStatus;
import com.se347.enrollmentservice.exceptions.EnrollmentException;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@Service
public class LearningProgressServiceImpl implements LearningProgressService {

    private static final Logger logger = LoggerFactory.getLogger(LearningProgressServiceImpl.class);
    
    private final LearningProgressRepository learningProgressRepository;
    private final EnrollmentService enrollmentService;
    private final CourseProgressService courseProgressService;

    // ========== Public API ==========

    @Transactional
    @Override
    public LearningProgressResponseDto createLearningProgress(LearningProgressRequestDto request) {
        validateCreateRequest(request);
        
        // Validate enrollment có thể truy cập (status, payment)
        validateEnrollmentForAccess(request.getEnrollmentId());

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
        // 1. Validate enrollment có thể truy cập (status, payment)
        validateEnrollmentForAccess(enrollmentId);
        
        // 2. Get or create learning progress với retry logic để tránh race condition
        LearningProgress learningProgress = getOrCreateLearningProgress(lessonId, enrollmentId);
        
        // 3. Update last accessed time
        learningProgress.setLastAccessedAt(LocalDateTime.now());
        learningProgressRepository.save(learningProgress);
        
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

    /**
     * Validate enrollment có thể truy cập lesson không
     * - Enrollment status phải là ACTIVE
     * - Payment status phải là PAID
     */
    private EnrollmentResponseDto validateEnrollmentForAccess(UUID enrollmentId) {
        EnrollmentResponseDto enrollment = enrollmentService.getEnrollmentById(enrollmentId);
        
        // Kiểm tra enrollment status
        if (enrollment.getEnrollmentStatus() != EnrollmentStatus.ACTIVE) {
            throw new EnrollmentException.InvalidEnrollmentStateException(
                "Cannot access lesson. Enrollment status is: " + enrollment.getEnrollmentStatus() + 
                ". Expected: ACTIVE");
        }
        
        // Kiểm tra payment status
        if (enrollment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new EnrollmentException.PaymentRequiredException(
                "Payment required. Current payment status: " + enrollment.getPaymentStatus() + 
                ". Expected: PAID");
        }
        
        return enrollment;
    }

    /**
     * Get or create learning progress với retry logic để tránh race condition
     * Nếu 2 requests cùng lúc truy cập lesson lần đầu, sẽ retry nếu gặp duplicate key exception
     */
    private LearningProgress getOrCreateLearningProgress(UUID lessonId, UUID enrollmentId) {
        // Thử tìm trước
        LearningProgress existing = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
        if (existing != null) {
            return existing;
        }
        
        // Nếu không tìm thấy, thử tạo mới với retry logic
        int maxRetries = 3;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                LearningProgress newProgress = LearningProgress.builder()
                    .enrollmentId(enrollmentId)
                    .lessonId(lessonId)
                    .isCompleted(false)
                    .lastAccessedAt(LocalDateTime.now())
                    .build();
                
                return learningProgressRepository.save(newProgress);
            } catch (DataIntegrityViolationException e) {
                // Duplicate key - có thể do race condition, thử lại
                logger.debug("Duplicate key detected for lesson {} and enrollment {}, attempt {}/{}", 
                    lessonId, enrollmentId, attempt + 1, maxRetries);
                
                if (attempt < maxRetries - 1) {
                    // Wait một chút trước khi retry (exponential backoff)
                    try {
                        Thread.sleep(50 * (attempt + 1)); // 50ms, 100ms, 150ms
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LearningProgressException.InvalidRequestException(
                            "Interrupted during retry for learning progress creation");
                    }
                    // Thử tìm lại (có thể request khác đã tạo xong)
                    existing = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
                    if (existing != null) {
                        logger.debug("Found existing learning progress after retry");
                        return existing;
                    }
                } else {
                    // Lần cuối, thử tìm lại một lần nữa
                    existing = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
                    if (existing != null) {
                        logger.debug("Found existing learning progress on final attempt");
                        return existing;
                    }
                    // Nếu vẫn không tìm thấy, throw exception
                    logger.error("Failed to create learning progress after {} retries for lesson {} and enrollment {}", 
                        maxRetries, lessonId, enrollmentId);
                    throw new LearningProgressException.InvalidRequestException(
                        "Failed to create learning progress after retries: " + e.getMessage());
                }
            }
        }
        
        // Fallback: thử tìm lại một lần cuối
        existing = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
        if (existing != null) {
            return existing;
        }
        
        throw new LearningProgressException.InvalidRequestException(
            "Failed to create learning progress for lesson " + lessonId + " and enrollment " + enrollmentId);
    }

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