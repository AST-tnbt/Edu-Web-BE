package com.se347.enrollmentservice.services.impl;

import com.se347.enrollmentservice.repositories.EnrollmentRepository;
import com.se347.enrollmentservice.services.EnrollmentQueryService;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.domains.EnrollmentAuthorizationDomainService;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.exceptions.EnrollmentException;
import com.se347.enrollmentservice.exceptions.ForbiddenException;
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

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponseDto getEnrollmentById(UUID enrollmentId, UUID userId) {

        if (enrollmentId == null) {
            throw new EnrollmentException.InvalidRequestException("Enrollment ID cannot be null");
        }

        if (userId == null) {
            throw new EnrollmentException.InvalidRequestException("User ID cannot be null");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));

        // Allow access if user is either the student OR the instructor
        try {
            enrollmentAuthorizationDomainService.ensureStudentOwnsEnrollment(enrollment, userId);
        } catch (ForbiddenException studentEx) {
            // If not student, check if user is the instructor
            try {
                if (enrollment.getInstructorId() != null) {
                    enrollmentAuthorizationDomainService.ensureInstructorOwnsCourse(enrollment.getCourseId(), userId);
                } else {
                    throw new ForbiddenException(
                        "User " + userId + " cannot access enrollment " + enrollmentId
                    );
                }
            } catch (ForbiddenException instructorEx) {
                throw new ForbiddenException(
                    "User " + userId + " cannot access enrollment " + enrollmentId
                );
            }
        }
        
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getEnrollmentsByStudentId(UUID studentId) {
        if (studentId == null) {
            throw new EnrollmentException.InvalidRequestException("Student ID cannot be null");
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        
        if (enrollments.isEmpty()) {
            return List.of();
        }
        
        // Verify authorization: ensure all enrollments belong to the student
        for (Enrollment enrollment : enrollments) {
            enrollmentAuthorizationDomainService.ensureStudentOwnsEnrollment(enrollment, studentId);
        }

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
        if (courseId == null) {
            throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        if (enrollments.isEmpty()) {
            return List.of();
        }
        
        // Get instructor ID from first enrollment to verify authorization
        UUID instructorId = enrollments.get(0).getInstructorId();
        if (instructorId != null) {
            enrollmentAuthorizationDomainService.ensureInstructorOwnsCourse(courseId, instructorId);
        }
        
        return enrollments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponseDto getEnrollmentByCourseIdAndStudentId(UUID courseId, UUID studentId) {
        if (courseId == null) {
            throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        }
        if (studentId == null) {
            throw new EnrollmentException.InvalidRequestException("Student ID cannot be null");
        }
        
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId);
        
        if (enrollment == null) {
            throw new EnrollmentException.EnrollmentNotFoundException(
                "Enrollment not found for course ID: " + courseId + " and student ID: " + studentId
            );
        }

        enrollmentAuthorizationDomainService.ensureStudentOwnsEnrollment(enrollment, studentId);
        if (enrollment.getInstructorId() != null) {
            enrollmentAuthorizationDomainService.ensureInstructorOwnsCourse(courseId, enrollment.getInstructorId());
        }

        return mapToResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getEnrollmentsByCourseIdInternal(UUID courseId) {
        // Internal method for system/event processing - no authorization check
        // Used by RabbitMQ listeners and internal services where authorization
        // has already been verified at the source (e.g., Course Service)
        if (courseId == null) {
            throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        }
        
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
