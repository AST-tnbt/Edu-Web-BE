package com.se347.enrollmentservice.domains;

import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.entities.CourseProgress;

import java.util.UUID;

public interface CourseProgressDomainService {
    // CourseProgress Entity CRUD operations (trả về entity)
    CourseProgress findCourseProgressById(UUID courseProgressId);
    CourseProgress findCourseProgressByEnrollmentId(UUID enrollmentId);
    boolean courseProgressExists(UUID courseProgressId);
    boolean courseProgressExistsByEnrollmentId(UUID enrollmentId);

    // Entity operations
    CourseProgress createCourseProgressEntity(CourseProgressRequestDto request);
    CourseProgress updateCourseProgressEntity(CourseProgress courseProgress, CourseProgressRequestDto request);
    CourseProgress patchCourseProgressEntity(CourseProgress courseProgress, CourseProgressRequestDto request);
    CourseProgress setTotalLessonsEntity(CourseProgress courseProgress, Integer totalLessons);

    // Business validations
    void validateCourseProgressCreation(CourseProgressRequestDto request);
    void validateCourseProgressUpdate(CourseProgress courseProgress, CourseProgressRequestDto request);
    void validateProgressRules(CourseProgress courseProgress, Integer originalLessonsCompleted, Integer originalTotalLessons);
    void validateEnrollmentExists(UUID enrollmentId);

    // Authorization checks
    boolean canUserAccessCourseProgress(CourseProgress courseProgress, UUID userId);

    // Business logic
    void calculateOverallProgress(CourseProgress courseProgress);
    void updateProgressAndCompletion(CourseProgress courseProgress);
}

