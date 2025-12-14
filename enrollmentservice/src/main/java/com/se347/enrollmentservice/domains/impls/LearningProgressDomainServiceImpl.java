package com.se347.enrollmentservice.domains.impls;

import com.se347.enrollmentservice.domains.LearningProgressDomainService;
import com.se347.enrollmentservice.dtos.LearningProgressRequestDto;
import com.se347.enrollmentservice.entities.LearningProgress;
import com.se347.enrollmentservice.exceptions.LearningProgressException;
import com.se347.enrollmentservice.repositories.LearningProgressRepository;
import com.se347.enrollmentservice.repositories.EnrollmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningProgressDomainServiceImpl implements LearningProgressDomainService {
    
    private final LearningProgressRepository learningProgressRepository;
    private final EnrollmentRepository enrollmentRepository;

    // ========== LearningProgress Entity CRUD operations ==========

    @Override
    @Transactional(readOnly = true)
    public LearningProgress findLearningProgressById(UUID learningProgressId) {
        if (learningProgressId == null) {
            throw new LearningProgressException.InvalidRequestException("Learning progress ID cannot be null");
        }
        return learningProgressRepository.findById(learningProgressId)
                .orElseThrow(() -> new LearningProgressException.LearningProgressNotFoundException(
                    "Learning progress not found with ID: " + learningProgressId));
    }

    @Override
    @Transactional(readOnly = true)
    public LearningProgress findLearningProgressByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId) {
        if (lessonId == null) {
            throw new LearningProgressException.InvalidRequestException("Lesson ID cannot be null");
        }
        if (enrollmentId == null) {
            throw new LearningProgressException.InvalidRequestException("Enrollment ID cannot be null");
        }
        LearningProgress learningProgress = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
        if (learningProgress == null) {
            throw new LearningProgressException.LearningProgressNotFoundException(
                "Learning progress not found for lesson " + lessonId + " and enrollment " + enrollmentId);
        }
        return learningProgress;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningProgress> findLearningProgressByEnrollmentId(UUID enrollmentId) {
        if (enrollmentId == null) {
            throw new LearningProgressException.InvalidRequestException("Enrollment ID cannot be null");
        }
        return learningProgressRepository.findByEnrollmentId(enrollmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean learningProgressExists(UUID learningProgressId) {
        if (learningProgressId == null) {
            return false;
        }
        return learningProgressRepository.existsById(learningProgressId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean learningProgressExistsByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId) {
        if (lessonId == null || enrollmentId == null) {
            return false;
        }
        return learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId) != null;
    }

    @Override
    @Transactional
    public LearningProgress getOrCreateLearningProgress(UUID lessonId, UUID enrollmentId) {
        if (lessonId == null) {
            throw new LearningProgressException.InvalidRequestException("Lesson ID cannot be null");
        }
        if (enrollmentId == null) {
            throw new LearningProgressException.InvalidRequestException("Enrollment ID cannot be null");
        }

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
                log.debug("Duplicate key detected for lesson {} and enrollment {}, attempt {}/{}", 
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
                        log.debug("Found existing learning progress after retry");
                        return existing;
                    }
                } else {
                    // Lần cuối, thử tìm lại một lần nữa
                    existing = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
                    if (existing != null) {
                        log.debug("Found existing learning progress on final attempt");
                        return existing;
                    }
                    // Nếu vẫn không tìm thấy, throw exception
                    log.error("Failed to create learning progress after {} retries for lesson {} and enrollment {}", 
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

    // ========== Entity operations ==========

    @Override
    public LearningProgress createLearningProgressEntity(LearningProgressRequestDto request) {
        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }
        
        LearningProgress learningProgress = LearningProgress.builder()
            .enrollmentId(request.getEnrollmentId())
            .lessonId(request.getLessonId())
            .isCompleted(false)
            .lastAccessedAt(LocalDateTime.now())
            .build();

        return learningProgress;
    }

    @Override
    public LearningProgress updateLearningProgressEntity(LearningProgress learningProgress, LearningProgressRequestDto request) {
        if (learningProgress == null) {
            throw new LearningProgressException.LearningProgressNotFoundException("Learning progress cannot be null");
        }
        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }

        // Store original completion status for validation
        boolean originalCompleted = learningProgress.isCompleted();

        // Apply updates
        learningProgress.setCompleted(request.isCompleted());
        learningProgress.setLastAccessedAt(LocalDateTime.now());

        // Handle completion status change
        updateCompletionStatus(learningProgress, originalCompleted);

        return learningProgress;
    }

    @Override
    public LearningProgress patchLearningProgressEntity(LearningProgress learningProgress, LearningProgressRequestDto request) {
        if (learningProgress == null) {
            throw new LearningProgressException.LearningProgressNotFoundException("Learning progress cannot be null");
        }
        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }

        boolean originalCompleted = learningProgress.isCompleted();

        // Update completed status if provided in request
        // For PATCH, we'll update if the value is different from current
        if (request.isCompleted() != learningProgress.isCompleted()) {
            learningProgress.setCompleted(request.isCompleted());
        }

        // Always update last accessed time
        learningProgress.setLastAccessedAt(LocalDateTime.now());

        // Handle completion status change
        updateCompletionStatus(learningProgress, originalCompleted);

        return learningProgress;
    }

    @Override
    public boolean canUserAccessLearningProgress(LearningProgress learningProgress, UUID userId) {
        if (learningProgress == null || userId == null) {
            return false;
        }
        return enrollmentRepository.findById(learningProgress.getEnrollmentId()).get().getStudentId().equals(userId);
    }

    @Override
    public LearningProgress markAsCompletedEntity(LearningProgress learningProgress) {
        if (learningProgress == null) {
            throw new LearningProgressException.LearningProgressNotFoundException("Learning progress cannot be null");
        }

        boolean originalCompleted = learningProgress.isCompleted();
        
        if (!learningProgress.isCompleted()) {
            learningProgress.setCompleted(true);
            learningProgress.setCompletedAt(LocalDateTime.now());
            learningProgress.setLastAccessedAt(LocalDateTime.now());
            updateCompletionStatus(learningProgress, originalCompleted);
        }

        return learningProgress;
    }

    @Override
    public LearningProgress updateLastAccessedEntity(LearningProgress learningProgress) {
        if (learningProgress == null) {
            throw new LearningProgressException.LearningProgressNotFoundException("Learning progress cannot be null");
        }

        learningProgress.setLastAccessedAt(LocalDateTime.now());
        return learningProgress;
    }

    // ========== Business validations ==========

    @Override
    public void validateLearningProgressCreation(LearningProgressRequestDto request) {
        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }
        if (request.getEnrollmentId() == null) {
            throw new LearningProgressException.InvalidRequestException("Enrollment ID cannot be null");
        }
        if (request.getLessonId() == null) {
            throw new LearningProgressException.InvalidRequestException("Lesson ID cannot be null");
        }
        
        // Check for duplicate learning progress
        validateNoDuplicateLearningProgress(request.getLessonId(), request.getEnrollmentId());
    }

    @Override
    public void validateLearningProgressUpdate(LearningProgress learningProgress, LearningProgressRequestDto request) {
        if (learningProgress == null) {
            throw new LearningProgressException.LearningProgressNotFoundException("Learning progress cannot be null");
        }
        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateNoDuplicateLearningProgress(UUID lessonId, UUID enrollmentId) {
        if (lessonId == null) {
            throw new LearningProgressException.InvalidRequestException("Lesson ID cannot be null");
        }
        if (enrollmentId == null) {
            throw new LearningProgressException.InvalidRequestException("Enrollment ID cannot be null");
        }
        
        LearningProgress existing = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
        if (existing != null) {
            throw new LearningProgressException.DuplicateLearningProgressException(
                "Learning progress already exists for lesson " + lessonId + 
                " and enrollment " + enrollmentId);
        }
    }

    // ========== Business logic ==========

    @Override
    public void updateCompletionStatus(LearningProgress learningProgress, boolean originalCompleted) {
        if (learningProgress == null) {
            throw new LearningProgressException.LearningProgressNotFoundException("Learning progress cannot be null");
        }

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

