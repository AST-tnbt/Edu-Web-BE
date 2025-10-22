package com.se347.enrollmentservice.services;

import com.se347.enrollmentservice.dtos.LearningProgressRequestDto;
import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;

import java.util.UUID;
import java.util.List;

public interface LearningProgressService {
    LearningProgressResponseDto createLearningProgress(LearningProgressRequestDto request);
    LearningProgressResponseDto getLearningProgressById(UUID learningProgressId);
    LearningProgressResponseDto getLearningProgressByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId);
    List<LearningProgressResponseDto> getLearningProgressByEnrollmentId(UUID enrollmentId);
    LearningProgressResponseDto updateLearningProgress(UUID learningProgressId, LearningProgressRequestDto request);
    // void deleteLearningProgress(UUID learningProgressId);
    void markLearningProgressAsCompleted(UUID lessonId, UUID enrollmentId);
}
