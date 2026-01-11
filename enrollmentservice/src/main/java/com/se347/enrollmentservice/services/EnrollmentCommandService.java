package com.se347.enrollmentservice.services;

import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;
import com.se347.enrollmentservice.enums.EnrollmentStatus;
import java.util.UUID;

public interface EnrollmentCommandService {

    /*
    Enrollment
     */
    EnrollmentResponseDto createEnrollment(EnrollmentRequestDto request);
    EnrollmentResponseDto updateEnrollmentStatus(UUID enrollmentId, EnrollmentStatus newStatus, UUID userId);
    
    /*
    Course Progress
     */

    void createCourseProgress(CourseProgressRequestDto request);
    void setTotalLessons(UUID courseProgressId, Integer totalLessons);

    /*
    Learning Progress
     */

    LearningProgressResponseDto getLearningProgressByEnrollmentIdAndLessonId(UUID enrollmentId, UUID lessonId, UUID userId);
    LearningProgressResponseDto markLessonAsCompleted(UUID lessonId, UUID enrollmentId, UUID userId);
    LearningProgressResponseDto recordLessonAccess(UUID lessonId, UUID enrollmentId, UUID userId);
}
