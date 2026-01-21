package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.enrollmentservice.exceptions.EnrollmentException;
import com.se347.enrollmentservice.services.EnrollmentCommandService;
import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.entities.LearningProgress;
import com.se347.enrollmentservice.repositories.EnrollmentRepository;
import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;
import com.se347.enrollmentservice.enums.EnrollmentStatus;
import com.se347.enrollmentservice.exceptions.CourseProgressException;
import com.se347.enrollmentservice.clients.CourseServiceClient;
import com.se347.enrollmentservice.exceptions.ForbiddenException;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class EnrollmentCommandServiceImpl implements EnrollmentCommandService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseServiceClient courseServiceClient;
    
    // ========== Public API ==========

    @Override
    @Transactional
    public EnrollmentResponseDto createEnrollment(EnrollmentRequestDto request) {
        
        Integer totalLessons = courseServiceClient.getTotalLessonsByCourseId(request.getCourseId());
        Enrollment enrollment = Enrollment.enroll(
            request.getCourseId(), 
            request.getCourseSlug(), 
            request.getStudentId(), 
            request.getInstructorId(),
            totalLessons);

        // Save through repository (infrastructure concern)
        enrollmentRepository.save(enrollment);
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponseDto updateEnrollmentStatus(UUID enrollmentId, EnrollmentStatus newStatus, UUID userId) {
        // Get enrollment through domain service
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));


        if (!enrollment.getInstructorId().equals(userId)) {
            throw new ForbiddenException(
                "User " + userId + " cannot update enrollment status " + enrollmentId + ": user is not the instructor"
            );
        }
        // Update status through domain service
        enrollment.suspend();
        enrollment.updateEnrollmentStatus(newStatus);

        // Save through repository (infrastructure concern)
        enrollmentRepository.save(enrollment);
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional
    public void createCourseProgress(CourseProgressRequestDto request) {
        // Course progress is created when enrollment is created
    }

    @Override
    @Transactional
    public void setTotalLessons(UUID enrollmentId, Integer totalLessons) {
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new CourseProgressException.CourseProgressNotFoundException("Course progress not found with ID: " + enrollmentId));
        
        enrollment.updateTotalLessons(totalLessons);
        enrollmentRepository.save(enrollment);
    }

    /*
    READ and WRITE Learning Progress
     */
    @Transactional
    @Override
    public LearningProgressResponseDto getLearningProgressByEnrollmentIdAndLessonId(UUID enrollmentId, UUID lessonId, UUID userId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));

        if (!enrollment.getStudentId().equals(userId)) {
            throw new ForbiddenException(
                "User " + userId + " cannot access learning progress " + enrollmentId + ": user is not the student"
            );
        }
        LearningProgress learningProgress = enrollment.getOrCreateLessonProgress(lessonId);
        learningProgress.recordAccess();
        enrollmentRepository.save(enrollment);
        return mapToResponse(learningProgress);
    }

    // Mark as completed by lesson and enrollment
    @Transactional
    @Override
    public LearningProgressResponseDto markLessonAsCompleted(UUID lessonId, UUID enrollmentId, UUID userId) {
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));

        if (!enrollment.getStudentId().equals(userId)) {
            throw new ForbiddenException(
                "User " + userId + " cannot mark lesson as completed " + enrollmentId + ": user is not the student"
            );
        }
        LearningProgress learningProgress = enrollment.markLessonAsCompleted(lessonId);
        enrollmentRepository.save(enrollment);
        return mapToResponse(learningProgress);
    }

    // Update last accessed time
    @Transactional
    @Override
    public LearningProgressResponseDto recordLessonAccess (UUID lessonId, UUID enrollmentId, UUID userId) {
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));

        if (!enrollment.getStudentId().equals(userId)) {
            throw new ForbiddenException(
                "User " + userId + " cannot record lesson access " + enrollmentId + ": user is not the student"
            );
        }
        LearningProgress learningProgress = enrollment.recordLessonAccess(lessonId);
        enrollmentRepository.save(enrollment);
        return mapToResponse(learningProgress);
    }

    // ========== Mapping ==========

    private EnrollmentResponseDto mapToResponse(Enrollment enrollment) {
        return EnrollmentResponseDto.builder()
            .enrollmentId(enrollment.getEnrollmentId())
            .courseId(enrollment.getCourseId())
            .courseSlug(enrollment.getCourseSlug())
            .studentId(enrollment.getStudentId())
            .enrolledAt(enrollment.getEnrolledAt())
            .enrollmentStatus(enrollment.getEnrollmentStatus())
            .createdAt(enrollment.getCreatedAt())
            .updatedAt(enrollment.getUpdatedAt())
            .build();
    }

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