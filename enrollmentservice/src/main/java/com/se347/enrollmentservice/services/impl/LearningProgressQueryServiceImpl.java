package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.enrollmentservice.services.LearningProgressQueryService;
import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;
import com.se347.enrollmentservice.repositories.LearningProgressRepository;
import com.se347.enrollmentservice.entities.LearningProgress;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.exceptions.LearningProgressException;
import com.se347.enrollmentservice.domains.EnrollmentAuthorizationDomainService;
import com.se347.enrollmentservice.exceptions.ForbiddenException;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LearningProgressQueryServiceImpl implements LearningProgressQueryService {
    
    private final LearningProgressRepository learningProgressRepository;
    private final EnrollmentAuthorizationDomainService enrollmentAuthorizationDomainService;

    // ========== Public API ==========

    @Override
    @Transactional(readOnly = true)
    public LearningProgressResponseDto getLearningProgressById(UUID learningProgressId, UUID userId) {
        LearningProgress learningProgress = learningProgressRepository.findByLearningProgressId(learningProgressId)
            .orElseThrow(() -> new LearningProgressException.LearningProgressNotFoundException("Learning progress not found with ID: " + learningProgressId));

        authorizeAccess(learningProgress, userId);
        
        return mapToResponse(learningProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningProgressResponseDto> getLearningProgressByEnrollmentId(UUID enrollmentId, UUID userId) {
        List<LearningProgress> learningProgresses = learningProgressRepository.findByEnrollmentId(enrollmentId)
            .orElseThrow(() -> new LearningProgressException.LearningProgressNotFoundException("Learning progress not found with enrollment ID: " + enrollmentId));

        authorizeAccess(learningProgresses.get(0), userId);

        return learningProgresses.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // ========== Private Helper Methods ==========

    private void authorizeAccess(LearningProgress learningProgress, UUID userId) {
            Enrollment enrollment = learningProgress.getEnrollment();
        try {
            enrollmentAuthorizationDomainService.ensureStudentOwnsEnrollment(enrollment, userId);
        } catch (ForbiddenException studentEx) {
            try {
                enrollmentAuthorizationDomainService.ensureInstructorOwnsCourse(
                    enrollment.getCourseId(), 
                    userId
                );
            } catch (ForbiddenException instructorEx) {
                throw new ForbiddenException(
                    "User " + userId + " cannot access learning progress " + learningProgress.getLearningProgressId()
                );
            }
        }
    }

    // ========== Mapping ==========

    private LearningProgressResponseDto mapToResponse(LearningProgress learningProgress) {
        return LearningProgressResponseDto.builder()
            .learningProgressId(learningProgress.getLearningProgressId())
            .enrollmentId(learningProgress.getEnrollment().getEnrollmentId())
            .lessonId(learningProgress.getLessonId())
            .isCompleted(learningProgress.isCompleted())
            .lastAccessedAt(learningProgress.getLastAccessedAt())
            .completedAt(learningProgress.getCompletedAt())
            .build();
    }
}