package com.se347.enrollmentservice.services.impl;

import com.se347.enrollmentservice.clients.CourseServiceClient;
import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;
import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.dtos.MyCourseProgressDto;
import com.se347.enrollmentservice.enums.EnrollmentStatus;
import com.se347.enrollmentservice.enums.PaymentStatus;
import com.se347.enrollmentservice.exceptions.CourseProgressException;
import com.se347.enrollmentservice.exceptions.EnrollmentException;
import com.se347.enrollmentservice.services.CourseProgressService;
import com.se347.enrollmentservice.services.EnrollmentDomainService;
import com.se347.enrollmentservice.services.EnrollmentService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentDomainServiceImpl implements EnrollmentDomainService {
    private final CourseServiceClient courseServiceClient;
    private final CourseProgressService courseProgressService;
    private final EnrollmentService enrollmentService;

    @Override
    public EnrollmentResponseDto createEnrollment(EnrollmentRequestDto request) {
        EnrollmentResponseDto enrollment = enrollmentService.createEnrollment(request);
        createCourseProgressForEnrollment(enrollment);
        return enrollment;
    }

    @Override
    public List<MyCourseProgressDto> getMyCourseProgress(UUID studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }

        List<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByStudentId(studentId);
        if (enrollments == null || enrollments.isEmpty()) {
            return Collections.emptyList();
        }

        return enrollments.stream()
            .map(this::buildMyCourseProgressDto)
            .collect(Collectors.toCollection(() -> new ArrayList<>(enrollments.size())));
    }

    private MyCourseProgressDto buildMyCourseProgressDto(EnrollmentResponseDto enrollment) {
        CourseProgressResponseDto progress = null;
        try {
            progress = courseProgressService.getCourseProgressByEnrollmentId(enrollment.getEnrollmentId());
        } catch (CourseProgressException.CourseProgressNotFoundException ex) {
            log.warn("Course progress not found for enrollment {}. Returning default progress.", enrollment.getEnrollmentId());
        }

        int lessonsCompleted = progress != null && progress.getLessonsCompleted() != null ? progress.getLessonsCompleted() : 0;
        int totalLessons = progress != null && progress.getTotalLessons() != null ? progress.getTotalLessons() : 0;
        double overallProgress = progress != null ? progress.getOverallProgress() : 0.0;
        boolean courseCompleted = progress != null && progress.isCourseCompleted();

        return MyCourseProgressDto.builder()
            .courseId(enrollment.getCourseId())
            .courseSlug(enrollment.getCourseSlug())
            .lessonsCompleted(lessonsCompleted)
            .totalLessons(totalLessons)
            .overallProgress(overallProgress)
            .courseCompleted(courseCompleted)
            .enrollmentStatus(enrollment.getEnrollmentStatus())
            .enrolledAt(enrollment.getEnrolledAt())
            .build();
    }

    private void createCourseProgressForEnrollment(EnrollmentResponseDto enrollment) {
        if (enrollment == null) {
            return;
        }

        UUID enrollmentId = enrollment.getEnrollmentId();
        UUID courseId = enrollment.getCourseId();

        int totalLessons = 0;
        try {
            Integer lessons = courseServiceClient.getTotalLessonsByCourseId(courseId);
            if (lessons != null) {
                totalLessons = lessons;
            }
        } catch (Exception ex) {
            log.warn("Failed to fetch total lessons for course {}. Defaulting to 0", courseId, ex);
        }

        CourseProgressRequestDto request = CourseProgressRequestDto.builder()
            .enrollmentId(enrollmentId)
            .lessonsCompleted(0)
            .totalLessons(totalLessons)
            .build();

        try {
            courseProgressService.createCourseProgress(request);
        } catch (Exception ex) {
            log.error("Failed to create course progress for enrollment {}", enrollmentId, ex);
            throw ex;
        }
    }

    @Override
    public void ensureEnrollmentAccessible(UUID enrollmentId) {
        EnrollmentResponseDto enrollment = enrollmentService.getEnrollmentById(enrollmentId);

        if (enrollment.getEnrollmentStatus() != EnrollmentStatus.ACTIVE) {
            throw new EnrollmentException.InvalidEnrollmentStateException(
                "Cannot access lesson. Enrollment status is: " + enrollment.getEnrollmentStatus());
        }

        if (enrollment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new EnrollmentException.PaymentRequiredException(
                "Payment required. Current payment status: " + enrollment.getPaymentStatus());
        }
    }

    @Override
    public void syncCourseProgressOnLessonChange(UUID enrollmentId, boolean increment) {
        try {
            CourseProgressResponseDto courseProgress = courseProgressService.getCourseProgressByEnrollmentId(enrollmentId);
            int currentLessonsCompleted = courseProgress.getLessonsCompleted();
            int newLessonsCompleted = increment
                ? currentLessonsCompleted + 1
                : Math.max(0, currentLessonsCompleted - 1);

            courseProgressService.patchCourseProgress(
                courseProgress.getCourseProgressId(),
                CourseProgressRequestDto.builder()
                    .lessonsCompleted(newLessonsCompleted)
                    .build()
            );
        } catch (CourseProgressException.CourseProgressNotFoundException ex) {
            log.warn("Course progress not found for enrollment {} when syncing lesson change", enrollmentId, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to sync course progress for enrollment {}", enrollmentId, ex);
            throw new RuntimeException("Failed to sync course progress: " + ex.getMessage(), ex);
        }
    }
}
