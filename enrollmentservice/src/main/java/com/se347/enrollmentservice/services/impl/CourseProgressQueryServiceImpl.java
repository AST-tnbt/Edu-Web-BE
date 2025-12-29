package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.enrollmentservice.entities.valueobjects.Percentage;
import com.se347.enrollmentservice.services.CourseProgressQueryService;
import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;
import com.se347.enrollmentservice.repositories.CourseProgressRepository;
import com.se347.enrollmentservice.entities.CourseProgress;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.exceptions.CourseProgressException;
import com.se347.enrollmentservice.domains.EnrollmentAuthorizationDomainService;
import com.se347.enrollmentservice.exceptions.ForbiddenException;

import lombok.RequiredArgsConstructor;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CourseProgressQueryServiceImpl implements CourseProgressQueryService {

    private final CourseProgressRepository courseProgressRepository;
    private final EnrollmentAuthorizationDomainService enrollmentAuthorizationDomainService;

    // ========== Public API ==========

    @Override
    @Transactional(readOnly = true)
    public CourseProgressResponseDto getCourseProgressById(UUID courseProgressId) {
        CourseProgress courseProgress = courseProgressRepository.findByCourseProgressId(courseProgressId)
            .orElseThrow(() -> new CourseProgressException.CourseProgressNotFoundException("Course progress not found with ID: " + courseProgressId));

        return mapToResponse(courseProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseProgressResponseDto getCourseProgressByEnrollmentId(UUID enrollmentId) {
        CourseProgress courseProgress = courseProgressRepository.findByEnrollmentId(enrollmentId)
            .orElseThrow(() -> new CourseProgressException.CourseProgressNotFoundException("Course progress not found with enrollment ID: " + enrollmentId));

        return mapToResponse(courseProgress);
    }

    // ========== Private Helper Methods ==========

    private void authorizeAccess(CourseProgress courseProgress, UUID userId) {
        Enrollment enrollment = courseProgress.getEnrollment();

        try {
            enrollmentAuthorizationDomainService.ensureStudentOwnsEnrollment(enrollment, userId);
        } catch (ForbiddenException studentEx) {
            try {
                enrollmentAuthorizationDomainService.ensureInstructorOwnsCourse(enrollment.getCourseId(), userId);
            } catch (ForbiddenException instructorEx) {
                throw new ForbiddenException("User " + userId + " cannot access course progress " + courseProgress.getCourseProgressId());
            }
        }
    }

    // ========== Mapping ==========

    private CourseProgressResponseDto mapToResponse(CourseProgress courseProgress) {
        return CourseProgressResponseDto.builder()
            .courseProgressId(courseProgress.getCourseProgressId())
            .overallProgress(Percentage.of(courseProgress.getProgressPercentage()))
            .lessonsCompleted(courseProgress.getLessonsCompleted())
            .totalLessons(courseProgress.getTotalLessons())
            .isAllLessonsCompleted(courseProgress.isAllLessonsCompleted())
            .allLessonsCompletedAt(courseProgress.getAllLessonsCompletedAt())
            .createdAt(courseProgress.getCreatedAt())
            .updatedAt(courseProgress.getUpdatedAt())
            .build();
    }

}