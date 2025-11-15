package com.se347.enrollmentservice.services;

import com.se347.enrollmentservice.dtos.LearningProgressRequestDto;
import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;

import java.util.UUID;
import java.util.List;

public interface LearningProgressService {
    LearningProgressResponseDto createLearningProgress(LearningProgressRequestDto request);
    LearningProgressResponseDto getLearningProgressById(UUID learningProgressId);
    List<LearningProgressResponseDto> getLearningProgressByEnrollmentId(UUID enrollmentId);
    LearningProgressResponseDto getLearningProgressByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId);
    LearningProgressResponseDto updateLearningProgress(UUID learningProgressId, LearningProgressRequestDto request);
    LearningProgressResponseDto patchLearningProgress(UUID learningProgressId, LearningProgressRequestDto request);
    LearningProgressResponseDto markAsCompleted(UUID lessonId, UUID enrollmentId);
    LearningProgressResponseDto updateLastAccessed(UUID learningProgressId);
}
