package com.se347.enrollmentservice.services;

import com.se347.enrollmentservice.dtos.LearningProgressRequestDto;
import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;

import java.util.UUID;
import java.util.List;

public interface LearningProgressService {
    LearningProgressResponseDto createLearningProgress(LearningProgressRequestDto request, UUID userId);
    LearningProgressResponseDto getLearningProgressById(UUID learningProgressId, UUID userId);
    List<LearningProgressResponseDto> getLearningProgressByEnrollmentId(UUID enrollmentId, UUID userId);
    LearningProgressResponseDto getLearningProgressByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId, UUID userId);
    LearningProgressResponseDto updateLearningProgress(UUID learningProgressId, LearningProgressRequestDto request, UUID userId);
    LearningProgressResponseDto patchLearningProgress(UUID learningProgressId, LearningProgressRequestDto request, UUID userId);
    LearningProgressResponseDto markAsCompleted(UUID lessonId, UUID enrollmentId);
    LearningProgressResponseDto updateLastAccessed(UUID learningProgressId);
}
