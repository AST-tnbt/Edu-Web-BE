package com.se347.enrollmentservice.services.impl;

import com.se347.enrollmentservice.repositories.EnrollmentRepository;
import com.se347.enrollmentservice.services.EnrollmentQueryService;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.domains.EnrollmentAuthorizationDomainService;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.exceptions.EnrollmentException;
import com.se347.enrollmentservice.clients.CourseServiceClient;
import java.util.UUID;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EnrollmentQueryServiceImpl implements EnrollmentQueryService {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentAuthorizationDomainService enrollmentAuthorizationDomainService;
    private final CourseServiceClient courseServiceClient;

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponseDto getEnrollmentById(UUID enrollmentId, UUID userId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));

        enrollmentAuthorizationDomainService.ensureStudentOwnsEnrollment(enrollment, userId);
        enrollmentAuthorizationDomainService.ensureInstructorOwnsCourse(enrollment.getCourseId(), userId);
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getEnrollmentsByStudentId(UUID studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        
        Enrollment enrollment = enrollments.stream().findFirst()
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("No enrollments found for student ID: " + studentId));
        enrollmentAuthorizationDomainService.ensureStudentOwnsEnrollment(enrollment, studentId);

        return enrollments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }


    public boolean isEnrollmentExists(UUID courseId, UUID studentId) {
        return enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnrollmentEmpty(UUID courseId) {   
        return !enrollmentRepository.existsByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getEnrollmentsByCourseId(UUID courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        Enrollment enrollment = enrollments.stream().findFirst()
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("No enrollments found for course ID: " + courseId));
        enrollmentAuthorizationDomainService.ensureInstructorOwnsCourse(courseId, enrollment.getStudentId());
        
        return enrollments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponseDto getEnrollmentByCourseIdAndStudentId(UUID courseId, UUID studentId) {
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId);

        enrollmentAuthorizationDomainService.ensureStudentOwnsEnrollment(enrollment, studentId);
        enrollmentAuthorizationDomainService.ensureInstructorOwnsCourse(courseId, courseServiceClient.getInstructorIdByCourseId(courseId));

        return mapToResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getEnrollmentsByCourseIdInternal(UUID courseId) {
        // Internal method for system/event processing - no authorization check
        // Used by RabbitMQ listeners and internal services where authorization
        // has already been verified at the source (e.g., Course Service)
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        
        return enrollments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
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
}
