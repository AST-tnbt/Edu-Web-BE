package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;
import com.se347.enrollmentservice.services.LearningProgressService;
import com.se347.enrollmentservice.dtos.LearningProgressRequestDto;
import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;
import com.se347.enrollmentservice.repositories.LearningProgressRepository;
import com.se347.enrollmentservice.entities.LearningProgress;
import com.se347.enrollmentservice.exceptions.LearningProgressException;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class LearningProgressServiceImpl implements LearningProgressService {

    private final LearningProgressRepository learningProgressRepository;

    public LearningProgressServiceImpl(LearningProgressRepository learningProgressRepository) {
        this.learningProgressRepository = learningProgressRepository;
    }

    @Override
    public LearningProgressResponseDto createLearningProgress(LearningProgressRequestDto request) {

        if (request == null) {
            throw new LearningProgressException.InvalidRequestException("Request cannot be null");
        }

        LearningProgress learningProgress = LearningProgress.builder()
            .enrollmentId(request.getEnrollmentId())
            .contentId(request.getContentId())
            .lessonId(request.getLessonId())
            .progressPercentage(request.getProgressPercentage())
            .timeSpent(request.getTimeSpent())
            .isCompleted(request.isCompleted())
            .build();

        learningProgressRepository.save(learningProgress);
        return mapToResponse(learningProgress);
    }

    @Override
    public LearningProgressResponseDto getLearningProgressById(UUID learningProgressId) {
        LearningProgress learningProgress = learningProgressRepository.findById(learningProgressId)
            .orElseThrow(() -> new LearningProgressException.LearningProgressNotFoundException(learningProgressId.toString()));
        return mapToResponse(learningProgress);
    }

    @Override
    public LearningProgressResponseDto getLearningProgressByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId) {
        LearningProgress learningProgress = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
        if (learningProgress == null) {
            throw new LearningProgressException.LearningProgressNotFoundException(lessonId.toString() + " and " + enrollmentId.toString());
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

        LearningProgress learningProgress = learningProgressRepository.findById(learningProgressId)
            .orElseThrow(() -> new LearningProgressException.LearningProgressNotFoundException(learningProgressId.toString()));

        learningProgress.setProgressPercentage(request.getProgressPercentage());
        learningProgress.setTimeSpent(request.getTimeSpent());
        learningProgress.setCompletedAt(LocalDateTime.now());
        learningProgress.setLastAccessedAt(LocalDateTime.now());
        return mapToResponse(learningProgressRepository.save(learningProgress));
    }

    @Override
    public void markLearningProgressAsCompleted(UUID lessonId, UUID enrollmentId) {
        LearningProgress learningProgress = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
        if (learningProgress == null) {
            throw new LearningProgressException.LearningProgressNotFoundException(lessonId.toString() + " and " + enrollmentId.toString());
        }

        learningProgress.setProgressPercentage(100);
        learningProgress.setCompletedAt(LocalDateTime.now());
        learningProgress.setCompleted(true);
        learningProgressRepository.save(learningProgress);
    }

    private LearningProgressResponseDto mapToResponse(LearningProgress learningProgress) {
        return LearningProgressResponseDto.builder()
            .learningProgressId(learningProgress.getLearningProgressId())
            .enrollmentId(learningProgress.getEnrollmentId())
            .contentId(learningProgress.getContentId())
            .lessonId(learningProgress.getLessonId())
            .progressPercentage(learningProgress.getProgressPercentage())
            .timeSpent(learningProgress.getTimeSpent())
            .isCompleted(learningProgress.isCompleted())
            .lastAccessedAt(learningProgress.getLastAccessedAt())
            .completedAt(learningProgress.getCompletedAt())
            .build();
    }
}