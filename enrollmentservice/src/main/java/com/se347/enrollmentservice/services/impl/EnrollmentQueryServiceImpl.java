package com.se347.enrollmentservice.services.impl;

import com.se347.enrollmentservice.repositories.EnrollmentRepository;
import com.se347.enrollmentservice.services.EnrollmentQueryService;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.exceptions.EnrollmentException;
import com.se347.enrollmentservice.exceptions.ForbiddenException;
import java.util.UUID;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class EnrollmentQueryServiceImpl implements EnrollmentQueryService {

    private final EnrollmentRepository enrollmentRepository;

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
            if (!enrollment.getStudentId().equals(userId)) {
                throw new ForbiddenException(
                    "User " + userId + " cannot access enrollment " + enrollmentId + ": user is not the student"
                );
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
            if (!enrollment.getStudentId().equals(studentId)) {
                throw new ForbiddenException(
                    "Student " + studentId + " cannot access enrollment " + enrollment.getEnrollmentId() + ": student is not the student"
                );
            }
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
    public List<EnrollmentResponseDto> getEnrollmentsByCourseId(UUID courseId, UUID userId) {
        if (courseId == null) {
            throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        }
        if (userId == null) {
            throw new EnrollmentException.InvalidRequestException("User ID cannot be null");
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        if (enrollments.isEmpty()) {
            return List.of();
        }
        
        // Only instructor of the course can view enrollments
        // Get instructor ID from first enrollment to verify authorization
        UUID instructorId = enrollments.get(0).getInstructorId();
        log.info("Instructor ID: {}", instructorId);
        log.info("User ID: {}", userId);
        
        if (instructorId != null) {
            if (!instructorId.equals(userId)) {
                throw new ForbiddenException(
                    "User " + userId + " cannot access enrollments for course " + courseId + ": user is not the instructor"
                );
            }
        } else {
            throw new ForbiddenException(
                "User " + userId + " cannot access enrollments for course " + courseId + ": course has no instructor"
            );
        }
        
        return enrollments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponseDto getEnrollmentByCourseIdAndStudentId(UUID courseId, UUID studentId, UUID userId) {
        if (courseId == null) {
            throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        }
        if (studentId == null) {
            throw new EnrollmentException.InvalidRequestException("Student ID cannot be null");
        }
        if (userId == null) {
            throw new EnrollmentException.InvalidRequestException("User ID cannot be null");
        }
        
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId);
        
        if (enrollment == null) {
            throw new EnrollmentException.EnrollmentNotFoundException(
                "Enrollment not found for course ID: " + courseId + " and student ID: " + studentId
            );
        }

        // Allow access if user is either the student OR the instructor of the course
        try {
            if (!enrollment.getStudentId().equals(userId)) {
                throw new ForbiddenException(
                    "User " + userId + " cannot access enrollment " + enrollment.getEnrollmentId() + ": user is not the student"
                );
            }
        } catch (ForbiddenException studentEx) {
            // If not student, check if user is the instructor
            try {
                if (enrollment.getInstructorId() != null) {
                    if (!enrollment.getInstructorId().equals(userId)) {
                        throw new ForbiddenException(
                            "User " + userId + " cannot access enrollment " + enrollment.getEnrollmentId() + ": user is not the instructor"
                        );
                    }
                } else {
                    throw new ForbiddenException(
                        "User " + userId + " cannot access enrollment for course " + courseId + " and student " + studentId
                    );
                }
            } catch (ForbiddenException instructorEx) {
                throw new ForbiddenException(
                    "User " + userId + " cannot access enrollment for course " + courseId + " and student " + studentId
                );
            }
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
