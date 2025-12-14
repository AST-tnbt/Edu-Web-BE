package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.enrollmentservice.services.CourseProgressService;
import com.se347.enrollmentservice.domains.CourseProgressDomainService;
import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;
import com.se347.enrollmentservice.repositories.CourseProgressRepository;
import com.se347.enrollmentservice.entities.CourseProgress;
import com.se347.enrollmentservice.exceptions.CourseProgressException;

import lombok.RequiredArgsConstructor;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CourseProgressServiceImpl implements CourseProgressService {

    private final CourseProgressRepository courseProgressRepository;
    private final CourseProgressDomainService courseProgressDomainService;
    
    // ========== Public API ==========

    @Override
    @Transactional
    public CourseProgressResponseDto createCourseProgress(CourseProgressRequestDto request) {
        // Validate business rules through domain service
        courseProgressDomainService.validateCourseProgressCreation(request);

        // Create entity through domain service
        CourseProgress courseProgress = courseProgressDomainService.createCourseProgressEntity(request);
        
        // Save through repository (infrastructure concern)
        courseProgressRepository.save(courseProgress);

        return mapToResponse(courseProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseProgressResponseDto getCourseProgressById(UUID courseProgressId) {
        return mapToResponse(courseProgressDomainService.findCourseProgressById(courseProgressId));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseProgressResponseDto getCourseProgressByEnrollmentId(UUID enrollmentId) {
        return mapToResponse(courseProgressDomainService.findCourseProgressByEnrollmentId(enrollmentId));
    }

    @Override
    @Transactional
    public CourseProgressResponseDto updateCourseProgress(UUID courseProgressId, CourseProgressRequestDto request, UUID userId) {
        // Get course progress through domain service
        CourseProgress courseProgress = courseProgressDomainService.findCourseProgressById(courseProgressId);
        if (!courseProgressDomainService.canUserAccessCourseProgress(courseProgress, userId)) {
            throw new CourseProgressException.UnauthorizedAccessException("User does not have access to this course progress");
        }
        // Validate business rules through domain service
        courseProgressDomainService.validateCourseProgressUpdate(courseProgress, request);

        // Update entity through domain service
        courseProgressDomainService.updateCourseProgressEntity(courseProgress, request);

        // Save through repository (infrastructure concern)
        return mapToResponse(courseProgressRepository.save(courseProgress));
    }

    // PATCH: Update only provided fields
    @Override
    @Transactional
    public CourseProgressResponseDto patchCourseProgress(UUID courseProgressId, CourseProgressRequestDto request) {
        if (courseProgressId == null) {
            throw new CourseProgressException.InvalidRequestException("Course progress ID cannot be null");
        }
        if (request == null) {
            throw new CourseProgressException.InvalidRequestException("Request cannot be null");
        }

        boolean hasAny = request.getLessonsCompleted() != null || request.getTotalLessons() != null;
        if (!hasAny) {
            throw new CourseProgressException.InvalidRequestException("At least one field must be provided for update");
        }

        // Validate provided fields
        if (request.getLessonsCompleted() != null && request.getLessonsCompleted() < 0) {
            throw new CourseProgressException.InvalidRequestException("Lessons completed must be non-negative");
        }
        if (request.getTotalLessons() != null && request.getTotalLessons() < 0) {
            throw new CourseProgressException.InvalidRequestException("Total lessons must be non-negative");
        }

        // Get course progress through domain service
        CourseProgress courseProgress = courseProgressDomainService.findCourseProgressById(courseProgressId);
        
        // Patch entity through domain service
        courseProgressDomainService.patchCourseProgressEntity(courseProgress, request);

        // Save through repository (infrastructure concern)
        return mapToResponse(courseProgressRepository.save(courseProgress));
    }

    @Override
    @Transactional
    public void setTotalLessons(UUID courseProgressId, Integer totalLessons) {
        if (courseProgressId == null) {
            throw new CourseProgressException.InvalidRequestException("Course progress ID cannot be null");
        }
        if (totalLessons == null || totalLessons < 0) {
            throw new CourseProgressException.InvalidRequestException("Total lessons must be non-negative");
        }
        
        // Get course progress through domain service
        CourseProgress courseProgress = courseProgressDomainService.findCourseProgressById(courseProgressId);
        
        // Set total lessons through domain service
        courseProgressDomainService.setTotalLessonsEntity(courseProgress, totalLessons);
        
        // Save through repository (infrastructure concern)
        courseProgressRepository.save(courseProgress);
    }

    // ========== Mapping ==========

    private CourseProgressResponseDto mapToResponse(CourseProgress courseProgress) {
        return CourseProgressResponseDto.builder()
            .courseProgressId(courseProgress.getCourseProgressId())
            .enrollmentId(courseProgress.getEnrollmentId())
            .overallProgress(courseProgress.getOverallProgress())
            .lessonsCompleted(courseProgress.getLessonsCompleted())
            .totalLessons(courseProgress.getTotalLessons())
            .isCourseCompleted(courseProgress.isCourseCompleted())
            .courseCompletedAt(courseProgress.getCourseCompletedAt())
            .createdAt(courseProgress.getCreatedAt())
            .updatedAt(courseProgress.getUpdatedAt())
            .build();
    }

}