package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.enrollmentservice.domains.CourseProgressDomainService;
import com.se347.enrollmentservice.exceptions.EnrollmentException;
import com.se347.enrollmentservice.clients.CourseServiceClient;
import com.se347.enrollmentservice.services.EnrollmentService;
import com.se347.enrollmentservice.domains.EnrollmentDomainService;
import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.entities.CourseProgress;
import com.se347.enrollmentservice.repositories.CourseProgressRepository;
import com.se347.enrollmentservice.repositories.EnrollmentRepository;
import com.se347.enrollmentservice.enums.EnrollmentStatus;
import com.se347.enrollmentservice.enums.PaymentStatus;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final CourseProgressDomainService courseProgressDomainService;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentDomainService enrollmentDomainService;
    private final CourseProgressRepository courseProgressRepository;
    private final CourseServiceClient courseServiceClient;
    // ========== Public API ==========

    @Override
    @Transactional
    public EnrollmentResponseDto createEnrollment(EnrollmentRequestDto request) {
        // Validate business rules through domain service
        enrollmentDomainService.validateEnrollmentCreation(request);

        // Create entity through domain service
        Enrollment enrollment = enrollmentDomainService.createEnrollmentEntity(request);
        
        // Save through repository (infrastructure concern)
        enrollmentRepository.save(enrollment);

        // Get total lessons from course service
        Integer totalLessons = courseServiceClient.getTotalLessonsByCourseId(enrollment.getCourseId());

        CourseProgressRequestDto courseProgressRequest = CourseProgressRequestDto.builder()
            .enrollmentId(enrollment.getEnrollmentId())
            .lessonsCompleted(0)
            .totalLessons(totalLessons)
            .build();

        // Validate business rules through domain service
        courseProgressDomainService.validateCourseProgressCreation(courseProgressRequest);

        // Create entity through domain service
        CourseProgress courseProgress = courseProgressDomainService.createCourseProgressEntity(courseProgressRequest);
        
        // Save through repository (infrastructure concern)
        courseProgressRepository.save(courseProgress);

        return mapToResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponseDto getEnrollmentById(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollmentDomainService.findEnrollmentById  (enrollmentId);
        
        if (!enrollmentDomainService.canInstructorAccessEnrollment(enrollment, userId)) {
            throw new EnrollmentException.UnauthorizedAccessException("User does not have access to this enrollment");
        }
        
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getEnrollmentsByStudentId(UUID studentId, UUID userId) {
        List<Enrollment> enrollments = enrollmentDomainService.findEnrollmentsByStudentId(studentId);
        for (Enrollment enrollment : enrollments) {
            if (!enrollmentDomainService.canUserAccessEnrollment(enrollment, userId)) {
                throw new EnrollmentException.UnauthorizedAccessException("User does not have access to this enrollment");
            }
        }
        return enrollments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getEnrollmentsByCourseId(UUID courseId) {
        List<Enrollment> enrollments = enrollmentDomainService.findEnrollmentsByCourseId(courseId);
        return enrollments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EnrollmentResponseDto updateEnrollment(UUID enrollmentId, EnrollmentRequestDto request, UUID userId) {
        // Get enrollment through domain service
        Enrollment enrollment = enrollmentDomainService.findEnrollmentById(enrollmentId);
        
        if (!enrollmentDomainService.canInstructorAccessEnrollment(enrollment, userId)) {
            throw new EnrollmentException.UnauthorizedAccessException("User does not have access to this enrollment");
        }
        
        // Validate business rules through domain service
        enrollmentDomainService.validateEnrollmentUpdate(enrollment, request);

        // Update entity through domain service
        enrollmentDomainService.updateEnrollmentEntity(enrollment, request);
        
        // Save through repository (infrastructure concern)
        enrollmentRepository.save(enrollment);
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnrollmentExists(UUID enrollmentId) {
        return enrollmentDomainService.enrollmentExists(enrollmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getEnrollmentsByCourseIdAndStudentId(UUID courseId, UUID studentId) {
        List<Enrollment> enrollments = enrollmentDomainService.findEnrollmentsByCourseIdAndStudentId(courseId, studentId);
        return enrollments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getMyCourses(UUID userId) {
        List<Enrollment> enrollments = enrollmentDomainService.findEnrollmentsByStudentId(userId);
        return enrollments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentDomainService.findAllEnrollments();
        return enrollments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // PATCH: chỉ cập nhật các field được cung cấp
    @Transactional
    public EnrollmentResponseDto patchEnrollment(UUID enrollmentId, EnrollmentRequestDto request, UUID userId) {
        if (enrollmentId == null) {
            throw new com.se347.enrollmentservice.exceptions.EnrollmentException.InvalidRequestException("Enrollment ID cannot be null");
        }
        if (request == null) {
            throw new com.se347.enrollmentservice.exceptions.EnrollmentException.InvalidRequestException("Request cannot be null");
        }

        Enrollment enrollment = enrollmentDomainService.findEnrollmentById(enrollmentId);
        if (!enrollmentDomainService.canUserAccessEnrollment(enrollment, userId)) {
            throw new EnrollmentException.UnauthorizedAccessException("User does not have access to this enrollment");
        }

        boolean hasAny =
            request.getCourseId() != null ||
            request.getStudentId() != null ||
            request.getEnrolledAt() != null ||
            request.getEnrollmentStatus() != null ||
            request.getPaymentStatus() != null;

        if (!hasAny) {
            throw new com.se347.enrollmentservice.exceptions.EnrollmentException.InvalidRequestException("At least one field must be provided for update");
        }

        // Patch entity through domain service
        enrollmentDomainService.patchEnrollmentEntity(enrollment, request);
        
        // Save through repository (infrastructure concern)
        enrollmentRepository.save(enrollment);
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponseDto updateEnrollmentStatus(UUID enrollmentId, EnrollmentStatus newStatus) {
        // Get enrollment through domain service
        Enrollment enrollment = enrollmentDomainService.findEnrollmentById(enrollmentId);
        
        // Update status through domain service
        enrollmentDomainService.updateEnrollmentStatusEntity(enrollment, newStatus);
        
        // Save through repository (infrastructure concern)
        enrollmentRepository.save(enrollment);
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponseDto updatePaymentStatus(UUID enrollmentId, PaymentStatus newStatus) {
        // Get enrollment through domain service
        Enrollment enrollment = enrollmentDomainService.findEnrollmentById(enrollmentId);
        
        // Update payment status through domain service
        enrollmentDomainService.updatePaymentStatusEntity(enrollment, newStatus);
        
        // Save through repository (infrastructure concern)
        enrollmentRepository.save(enrollment);
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponseDto updateStatuses(UUID enrollmentId, EnrollmentStatus enrollmentStatus, PaymentStatus paymentStatus) {
        // Get enrollment through domain service
        Enrollment enrollment = enrollmentDomainService.findEnrollmentById(enrollmentId);
        
        // Update statuses through domain service
        enrollmentDomainService.updateStatusesEntity(enrollment, enrollmentStatus, paymentStatus);
        
        // Save through repository (infrastructure concern)
        enrollmentRepository.save(enrollment);
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public Enrollment toEnrollment(UUID enrollmentId) {
        return enrollmentDomainService.toEnrollment(enrollmentId);
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
            .paymentStatus(enrollment.getPaymentStatus())
            .createdAt(enrollment.getCreatedAt())
            .updatedAt(enrollment.getUpdatedAt())
            .build();
    }
}