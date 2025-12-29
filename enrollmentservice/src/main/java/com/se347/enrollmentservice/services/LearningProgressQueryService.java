package com.se347.enrollmentservice.services;

import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;

import java.util.UUID;
import java.util.List;

public interface LearningProgressQueryService {
    LearningProgressResponseDto getLearningProgressById(UUID learningProgressId, UUID userId);
    List<LearningProgressResponseDto> getLearningProgressByEnrollmentId(UUID enrollmentId, UUID userId);
}
