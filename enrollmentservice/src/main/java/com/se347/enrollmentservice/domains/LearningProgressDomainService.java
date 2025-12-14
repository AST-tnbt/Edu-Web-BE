package com.se347.enrollmentservice.domains;

import com.se347.enrollmentservice.dtos.LearningProgressRequestDto;
import com.se347.enrollmentservice.entities.LearningProgress;

import java.util.List;
import java.util.UUID;

public interface LearningProgressDomainService {
    // LearningProgress Entity CRUD operations (trả về entity)
    LearningProgress findLearningProgressById(UUID learningProgressId);
    LearningProgress findLearningProgressByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId);
    List<LearningProgress> findLearningProgressByEnrollmentId(UUID enrollmentId);
    boolean learningProgressExists(UUID learningProgressId);
    boolean learningProgressExistsByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId);
    LearningProgress getOrCreateLearningProgress(UUID lessonId, UUID enrollmentId);

    // Entity operations
    LearningProgress createLearningProgressEntity(LearningProgressRequestDto request);
    LearningProgress updateLearningProgressEntity(LearningProgress learningProgress, LearningProgressRequestDto request);
    LearningProgress patchLearningProgressEntity(LearningProgress learningProgress, LearningProgressRequestDto request);
    LearningProgress markAsCompletedEntity(LearningProgress learningProgress);
    LearningProgress updateLastAccessedEntity(LearningProgress learningProgress);

    // Authorization checks
    boolean canUserAccessLearningProgress(LearningProgress learningProgress, UUID userId);

    // Business validations
    void validateLearningProgressCreation(LearningProgressRequestDto request);
    void validateLearningProgressUpdate(LearningProgress learningProgress, LearningProgressRequestDto request);
    void validateNoDuplicateLearningProgress(UUID lessonId, UUID enrollmentId);

    // Business logic
    void updateCompletionStatus(LearningProgress learningProgress, boolean originalCompleted);
}

