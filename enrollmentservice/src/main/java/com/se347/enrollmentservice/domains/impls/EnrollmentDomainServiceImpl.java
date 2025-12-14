package com.se347.enrollmentservice.domains.impls;

import com.se347.enrollmentservice.clients.CourseServiceClient;
import com.se347.enrollmentservice.domains.EnrollmentDomainService;
import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;
import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.dtos.MyCourseProgressDto;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.enums.EnrollmentStatus;
import com.se347.enrollmentservice.enums.PaymentStatus;
import com.se347.enrollmentservice.exceptions.CourseProgressException;
import com.se347.enrollmentservice.exceptions.EnrollmentException;
import com.se347.enrollmentservice.repositories.EnrollmentRepository;
import com.se347.enrollmentservice.services.CourseProgressService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentDomainServiceImpl implements EnrollmentDomainService {
    private final CourseServiceClient courseServiceClient;
    private final CourseProgressService courseProgressService;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public EnrollmentResponseDto createEnrollment(EnrollmentRequestDto request) {
        // Validate business rules
        validateEnrollmentCreation(request);
        
        // Create entity
        Enrollment enrollment = createEnrollmentEntity(request);
        
        // Save through repository
        enrollmentRepository.save(enrollment);
        
        // Map to DTO
        EnrollmentResponseDto enrollmentDto = mapToResponse(enrollment);
        
        return enrollmentDto;
    }
    
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

    @Override
    public List<MyCourseProgressDto> getMyCourseProgress(UUID studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }

        List<Enrollment> enrollmentEntities = findEnrollmentsByStudentId(studentId);
        List<EnrollmentResponseDto> enrollments = enrollmentEntities.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
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

    @Override
    @Transactional(readOnly = true)
    public void ensureEnrollmentAccessible(UUID enrollmentId) {
        Enrollment enrollment = findEnrollmentById(enrollmentId);

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

    // ========== Enrollment Entity CRUD operations ==========

    @Override
    @Transactional(readOnly = true)
    public Enrollment findEnrollmentById(UUID enrollmentId) {
        if (enrollmentId == null) {
            throw new EnrollmentException.InvalidRequestException("Enrollment ID cannot be null");
        }
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException(enrollmentId.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public Enrollment toEnrollment(UUID enrollmentId) {
        return findEnrollmentById(enrollmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean enrollmentExists(UUID enrollmentId) {
        if (enrollmentId == null) {
            return false;
        }
        return enrollmentRepository.existsById(enrollmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> findEnrollmentsByStudentId(UUID studentId) {
        if (studentId == null) {
            throw new EnrollmentException.InvalidRequestException("Student ID cannot be null");
        }
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> findEnrollmentsByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        }
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> findEnrollmentsByCourseIdAndStudentId(UUID courseId, UUID studentId) {
        if (courseId == null) {
            throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        }
        if (studentId == null) {
            throw new EnrollmentException.InvalidRequestException("Student ID cannot be null");
        }
        return enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> findAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    // ========== Entity operations ==========

    @Override
    public Enrollment createEnrollmentEntity(EnrollmentRequestDto request) {
        if (request == null) {
            throw new EnrollmentException.InvalidRequestException("Request cannot be null");
        }
        
        Enrollment enrollment = Enrollment.builder()
            .courseId(request.getCourseId())
            .courseSlug(request.getCourseSlug())
            .studentId(request.getStudentId())
            .enrolledAt(LocalDateTime.now())
            .enrollmentStatus(request.getEnrollmentStatus())
            .paymentStatus(request.getPaymentStatus())
            .build();

        enrollment.onCreate();
        enrollment.onUpdate();
        return enrollment;
    }

    @Override
    public Enrollment updateEnrollmentEntity(Enrollment enrollment, EnrollmentRequestDto request) {
        if (enrollment == null) {
            throw new EnrollmentException.EnrollmentNotFoundException("Enrollment cannot be null");
        }
        if (request == null) {
            throw new EnrollmentException.InvalidRequestException("Request cannot be null");
        }
        
        enrollment.setCourseId(request.getCourseId());
        enrollment.setCourseSlug(request.getCourseSlug());
        enrollment.setStudentId(request.getStudentId());
        enrollment.setEnrolledAt(request.getEnrolledAt() != null ? request.getEnrolledAt() : enrollment.getEnrolledAt());
        enrollment.setEnrollmentStatus(request.getEnrollmentStatus() != null ? request.getEnrollmentStatus() : enrollment.getEnrollmentStatus());
        enrollment.setPaymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : enrollment.getPaymentStatus());
        enrollment.onUpdate();
        
        return enrollment;
    }

    @Override
    public Enrollment patchEnrollmentEntity(Enrollment enrollment, EnrollmentRequestDto request) {
        if (enrollment == null) {
            throw new EnrollmentException.EnrollmentNotFoundException("Enrollment cannot be null");
        }
        if (request == null) {
            throw new EnrollmentException.InvalidRequestException("Request cannot be null");
        }
        
        EnrollmentStatus oldStatus = enrollment.getEnrollmentStatus();
        PaymentStatus oldPayment = enrollment.getPaymentStatus();
        UUID oldCourseId = enrollment.getCourseId();
        UUID oldStudentId = enrollment.getStudentId();
        
        // Apply partial update
        if (request.getCourseId() != null) enrollment.setCourseId(request.getCourseId());
        if (request.getCourseSlug() != null) enrollment.setCourseSlug(request.getCourseSlug());
        if (request.getStudentId() != null) enrollment.setStudentId(request.getStudentId());
        if (request.getEnrolledAt() != null) enrollment.setEnrolledAt(request.getEnrolledAt());
        if (request.getEnrollmentStatus() != null) enrollment.setEnrollmentStatus(request.getEnrollmentStatus());
        if (request.getPaymentStatus() != null) enrollment.setPaymentStatus(request.getPaymentStatus());
        
        // Validate if keys changed
        if (!oldCourseId.equals(enrollment.getCourseId()) || !oldStudentId.equals(enrollment.getStudentId())) {
            validateNoDuplicateEnrollment(enrollment.getEnrollmentId(), enrollment.getCourseId(), enrollment.getStudentId());
        }
        if (!oldStatus.equals(enrollment.getEnrollmentStatus())) {
            validateStatusTransition(oldStatus, enrollment.getEnrollmentStatus());
        }
        if (!oldPayment.equals(enrollment.getPaymentStatus())) {
            validatePaymentStatusTransition(oldPayment, enrollment.getPaymentStatus());
        }
        
        enrollment.onUpdate();
        return enrollment;
    }

    @Override
    public Enrollment updateEnrollmentStatusEntity(Enrollment enrollment, EnrollmentStatus newStatus) {
        if (enrollment == null) {
            throw new EnrollmentException.EnrollmentNotFoundException("Enrollment cannot be null");
        }
        if (newStatus == null) {
            throw new EnrollmentException.InvalidRequestException("Enrollment status cannot be null");
        }
        
        validateStatusTransition(enrollment.getEnrollmentStatus(), newStatus);
        enrollment.setEnrollmentStatus(newStatus);
        enrollment.onUpdate();
        
        return enrollment;
    }

    @Override
    public Enrollment updatePaymentStatusEntity(Enrollment enrollment, PaymentStatus newStatus) {
        if (enrollment == null) {
            throw new EnrollmentException.EnrollmentNotFoundException("Enrollment cannot be null");
        }
        if (newStatus == null) {
            throw new EnrollmentException.InvalidRequestException("Payment status cannot be null");
        }
        
        validatePaymentStatusTransition(enrollment.getPaymentStatus(), newStatus);
        enrollment.setPaymentStatus(newStatus);
        enrollment.onUpdate();
        
        return enrollment;
    }

    @Override
    public Enrollment updateStatusesEntity(Enrollment enrollment, EnrollmentStatus enrollmentStatus, PaymentStatus paymentStatus) {
        if (enrollment == null) {
            throw new EnrollmentException.EnrollmentNotFoundException("Enrollment cannot be null");
        }
        
        if (enrollmentStatus != null && !enrollment.getEnrollmentStatus().equals(enrollmentStatus)) {
            validateStatusTransition(enrollment.getEnrollmentStatus(), enrollmentStatus);
            enrollment.setEnrollmentStatus(enrollmentStatus);
        }
        if (paymentStatus != null && !enrollment.getPaymentStatus().equals(paymentStatus)) {
            validatePaymentStatusTransition(enrollment.getPaymentStatus(), paymentStatus);
            enrollment.setPaymentStatus(paymentStatus);
        }
        
        enrollment.onUpdate();
        return enrollment;
    }

    // ========== Business validations ==========

    @Override
    public void validateEnrollmentCreation(EnrollmentRequestDto request) {
        if (request == null) {
            throw new EnrollmentException.InvalidRequestException("Request cannot be null");
        }
        if (request.getCourseId() == null) {
            throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        }
        if (request.getStudentId() == null) {
            throw new EnrollmentException.InvalidRequestException("Student ID cannot be null");
        }
        
        // Validate no duplicate enrollment
        validateNoDuplicateEnrollment(null, request.getCourseId(), request.getStudentId());
    }


    @Override
    public boolean canUserAccessEnrollment(Enrollment enrollment, UUID userId) {
        if (enrollment == null || userId == null) {
            return false;
        }
        
        return enrollment.getStudentId().equals(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canInstructorAccessEnrollment(Enrollment enrollment, UUID instructorId) {
        if (enrollment == null || instructorId == null) {
            return false;
        }
        
        try {
            UUID courseInstructorId = courseServiceClient.getInstructorIdByCourseId(enrollment.getCourseId());
            return courseInstructorId.equals(instructorId);
        } catch (Exception e) {
            log.error("Failed to verify instructor access for enrollment {}", enrollment.getEnrollmentId(), e);
            return false;
        }
    }

    @Override
    public void validateEnrollmentUpdate(Enrollment enrollment, EnrollmentRequestDto request) {
        if (enrollment == null) {
            throw new EnrollmentException.EnrollmentNotFoundException("Enrollment cannot be null");
        }
        if (request == null) {
            throw new EnrollmentException.InvalidRequestException("Request cannot be null");
        }
        if (request.getCourseId() == null) {
            throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        }
        if (request.getStudentId() == null) {
            throw new EnrollmentException.InvalidRequestException("Student ID cannot be null");
        }
        if (request.getEnrollmentStatus() == null) {
            throw new EnrollmentException.InvalidRequestException("Enrollment status cannot be null");
        }
        if (request.getPaymentStatus() == null) {
            throw new EnrollmentException.InvalidRequestException("Payment status cannot be null");
        }
        
        // Validate no duplicate if keys changed
        if (!enrollment.getCourseId().equals(request.getCourseId()) || 
            !enrollment.getStudentId().equals(request.getStudentId())) {
            validateNoDuplicateEnrollment(enrollment.getEnrollmentId(), request.getCourseId(), request.getStudentId());
        }
        
        // Validate status transitions
        validateStatusTransition(enrollment.getEnrollmentStatus(), request.getEnrollmentStatus());
        validatePaymentStatusTransition(enrollment.getPaymentStatus(), request.getPaymentStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateNoDuplicateEnrollment(UUID currentEnrollmentId, UUID courseId, UUID studentId) {
        if (courseId == null) {
            throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        }
        if (studentId == null) {
            throw new EnrollmentException.InvalidRequestException("Student ID cannot be null");
        }
        
        boolean duplicateExists = enrollmentRepository
            .findByCourseIdAndStudentId(courseId, studentId)
            .stream()
            .anyMatch(e -> currentEnrollmentId == null || !e.getEnrollmentId().equals(currentEnrollmentId));

        if (duplicateExists) {
            throw new EnrollmentException.DuplicateEnrollmentException("Student is already enrolled in this course");
        }
    }

    @Override
    public void validateStatusTransition(EnrollmentStatus currentStatus, EnrollmentStatus newStatus) {
        if (newStatus == null) {
            throw new EnrollmentException.InvalidStatusTransitionException("New status cannot be null");
        }

        switch (currentStatus) {
            case ACTIVE:
                if (newStatus == EnrollmentStatus.COMPLETED || 
                    newStatus == EnrollmentStatus.CANCELLED || 
                    newStatus == EnrollmentStatus.SUSPENDED) {
                    return;
                }
                break;
            case SUSPENDED:
                if (newStatus == EnrollmentStatus.ACTIVE || 
                    newStatus == EnrollmentStatus.CANCELLED) {
                    return;
                }
                break;
            case COMPLETED:
            case CANCELLED:
                throw new EnrollmentException.InvalidStatusTransitionException(
                    "Cannot change status from " + currentStatus + " to " + newStatus);
        }
        throw new EnrollmentException.InvalidStatusTransitionException(
            "Invalid status transition from " + currentStatus + " to " + newStatus);
    }

    @Override
    public void validatePaymentStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        if (newStatus == null) {
            throw new EnrollmentException.InvalidPaymentStatusTransitionException("New payment status cannot be null");
        }

        switch (currentStatus) {
            case PENDING:
                if (newStatus == PaymentStatus.PAID || 
                    newStatus == PaymentStatus.REFUNDED || 
                    newStatus == PaymentStatus.CANCELLED) {
                    return;
                }
                break;
            case PAID:
                if (newStatus == PaymentStatus.REFUNDED) {
                    return;
                }
                break;
            case REFUNDED:
            case CANCELLED:
                throw new EnrollmentException.InvalidPaymentStatusTransitionException(
                    "Cannot change payment status from " + currentStatus + " to " + newStatus);
        }
        throw new EnrollmentException.InvalidPaymentStatusTransitionException(
            "Invalid payment status transition from " + currentStatus + " to " + newStatus);
    }
}
