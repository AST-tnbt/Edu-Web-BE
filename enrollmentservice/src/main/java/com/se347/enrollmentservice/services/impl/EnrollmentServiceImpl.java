package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;

import com.se347.enrollmentservice.services.EnrollmentService;
import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.repositories.EnrollmentRepository;
import com.se347.enrollmentservice.enums.EnrollmentStatus;
import com.se347.enrollmentservice.enums.PaymentStatus;
import com.se347.enrollmentservice.exceptions.EnrollmentException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    // ========== Public API ==========

    @Override
    public EnrollmentResponseDto createEnrollment(EnrollmentRequestDto request) {
        validateCreateRequest(request);

        Enrollment enrollment = Enrollment.builder()
            .courseId(request.getCourseId())
            .studentId(request.getStudentId())
            .enrolledAt(request.getEnrolledAt())
            .enrollmentStatus(request.getEnrollmentStatus())
            .paymentStatus(request.getPaymentStatus())
            .build();

        enrollment.onCreate();
        enrollment.onUpdate();
        enrollmentRepository.save(enrollment);
        return mapToResponse(enrollment);
    }

    @Override
    public EnrollmentResponseDto getEnrollmentById(UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException(enrollmentId.toString()));
        return mapToResponse(enrollment);
    }

    @Override
    public List<EnrollmentResponseDto> getEnrollmentsByStudentId(UUID studentId) {
        return enrollmentRepository.findByStudentId(studentId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentResponseDto> getEnrollmentsByCourseId(UUID courseId) {
        return enrollmentRepository.findByCourseId(courseId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public EnrollmentResponseDto updateEnrollment(UUID enrollmentId, EnrollmentRequestDto request) {
        validateUpdateRequest(enrollmentId, request);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));

        validateNoDuplicateIfKeysChanged(enrollment, request);
        validateStatusTransition(enrollment.getEnrollmentStatus(), request.getEnrollmentStatus());
        validatePaymentStatusTransition(enrollment.getPaymentStatus(), request.getPaymentStatus());

        applyFullUpdate(enrollment, request);

        enrollment.onUpdate();
        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public boolean isEnrollmentExists(UUID enrollmentId) {
        return enrollmentRepository.existsById(enrollmentId);
    }

    @Override
    public List<EnrollmentResponseDto> getEnrollmentsByCourseIdAndStudentId(UUID courseId, UUID studentId) {
        return enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentResponseDto> getAllEnrollments() {
        return enrollmentRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // PATCH: chỉ cập nhật các field được cung cấp
    public EnrollmentResponseDto patchEnrollment(UUID enrollmentId, EnrollmentRequestDto request) {
        validatePatchRequest(enrollmentId, request);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));

        EnrollmentStatus oldStatus = enrollment.getEnrollmentStatus();
        PaymentStatus oldPayment = enrollment.getPaymentStatus();
        UUID oldCourseId = enrollment.getCourseId();
        UUID oldStudentId = enrollment.getStudentId();

        applyPartialUpdate(enrollment, request);

        if (!oldCourseId.equals(enrollment.getCourseId()) || !oldStudentId.equals(enrollment.getStudentId())) {
            validateNoDuplicate(enrollment.getEnrollmentId(), enrollment.getCourseId(), enrollment.getStudentId());
        }
        if (!oldStatus.equals(enrollment.getEnrollmentStatus())) {
            validateStatusTransition(oldStatus, enrollment.getEnrollmentStatus());
        }
        if (!oldPayment.equals(enrollment.getPaymentStatus())) {
            validatePaymentStatusTransition(oldPayment, enrollment.getPaymentStatus());
        }

        enrollment.onUpdate();
        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    public EnrollmentResponseDto updateEnrollmentStatus(UUID enrollmentId, EnrollmentStatus newStatus) {
        if (enrollmentId == null) throw new EnrollmentException.InvalidRequestException("Enrollment ID cannot be null");
        if (newStatus == null) throw new EnrollmentException.InvalidRequestException("Enrollment status cannot be null");

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));

        validateStatusTransition(enrollment.getEnrollmentStatus(), newStatus);
        enrollment.setEnrollmentStatus(newStatus);
        enrollment.onUpdate();

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    public EnrollmentResponseDto updatePaymentStatus(UUID enrollmentId, PaymentStatus newStatus) {
        if (enrollmentId == null) throw new EnrollmentException.InvalidRequestException("Enrollment ID cannot be null");
        if (newStatus == null) throw new EnrollmentException.InvalidRequestException("Payment status cannot be null");

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));

        validatePaymentStatusTransition(enrollment.getPaymentStatus(), newStatus);
        enrollment.setPaymentStatus(newStatus);
        enrollment.onUpdate();

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    public EnrollmentResponseDto updateStatuses(UUID enrollmentId, EnrollmentStatus enrollmentStatus, PaymentStatus paymentStatus) {
        if (enrollmentId == null) throw new EnrollmentException.InvalidRequestException("Enrollment ID cannot be null");

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException("Enrollment not found with ID: " + enrollmentId));

        if (enrollmentStatus != null && !enrollment.getEnrollmentStatus().equals(enrollmentStatus)) {
            validateStatusTransition(enrollment.getEnrollmentStatus(), enrollmentStatus);
            enrollment.setEnrollmentStatus(enrollmentStatus);
        }
        if (paymentStatus != null && !enrollment.getPaymentStatus().equals(paymentStatus)) {
            validatePaymentStatusTransition(enrollment.getPaymentStatus(), paymentStatus);
            enrollment.setPaymentStatus(paymentStatus);
        }

        enrollment.onUpdate();
        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    // ========== Mapping ==========

    private EnrollmentResponseDto mapToResponse(Enrollment enrollment) {
        return EnrollmentResponseDto.builder()
            .enrollmentId(enrollment.getEnrollmentId())
            .courseId(enrollment.getCourseId())
            .studentId(enrollment.getStudentId())
            .enrolledAt(enrollment.getEnrolledAt())
            .enrollmentStatus(enrollment.getEnrollmentStatus())
            .paymentStatus(enrollment.getPaymentStatus())
            .createdAt(enrollment.getCreatedAt())
            .updatedAt(enrollment.getUpdatedAt())
            .build();
    }

    // ========== Validation: request ==========

    private void validateCreateRequest(EnrollmentRequestDto request) {
        if (request == null) throw new EnrollmentException.InvalidRequestException("Request cannot be null");
        if (request.getCourseId() == null) throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        if (request.getStudentId() == null) throw new EnrollmentException.InvalidRequestException("Student ID cannot be null");
        // EnrollmentStatus/PaymentStatus có thể null khi tạo (tùy nghiệp vụ), giữ nguyên theo code hiện tại
    }

    private void validateUpdateRequest(UUID enrollmentId, EnrollmentRequestDto request) {
        if (enrollmentId == null) throw new EnrollmentException.InvalidRequestException("Enrollment ID cannot be null");
        if (request == null) throw new EnrollmentException.InvalidRequestException("Request cannot be null");
        if (request.getCourseId() == null) throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        if (request.getStudentId() == null) throw new EnrollmentException.InvalidRequestException("Student ID cannot be null");
        if (request.getEnrollmentStatus() == null) throw new EnrollmentException.InvalidRequestException("Enrollment status cannot be null");
        if (request.getPaymentStatus() == null) throw new EnrollmentException.InvalidRequestException("Payment status cannot be null");
    }

    private void validatePatchRequest(UUID enrollmentId, EnrollmentRequestDto request) {
        if (enrollmentId == null) throw new EnrollmentException.InvalidRequestException("Enrollment ID cannot be null");
        if (request == null) throw new EnrollmentException.InvalidRequestException("Request cannot be null");

        boolean hasAny =
            request.getCourseId() != null ||
            request.getStudentId() != null ||
            request.getEnrolledAt() != null ||
            request.getEnrollmentStatus() != null ||
            request.getPaymentStatus() != null;

        if (!hasAny) throw new EnrollmentException.InvalidRequestException("At least one field must be provided for update");
    }

    // ========== Validation: business rules ==========

    private void validateNoDuplicateIfKeysChanged(Enrollment existing, EnrollmentRequestDto request) {
        if (!existing.getCourseId().equals(request.getCourseId()) || !existing.getStudentId().equals(request.getStudentId())) {
            validateNoDuplicate(existing.getEnrollmentId(), request.getCourseId(), request.getStudentId());
        }
    }

    private void validateNoDuplicate(UUID currentId, UUID courseId, UUID studentId) {
        boolean duplicateExists = enrollmentRepository
            .findByCourseIdAndStudentId(courseId, studentId)
            .stream()
            .anyMatch(e -> !e.getEnrollmentId().equals(currentId));

        if (duplicateExists) {
            throw new EnrollmentException.DuplicateEnrollmentException("Student is already enrolled in this course");
        }
    }

    private void validateStatusTransition(EnrollmentStatus currentStatus, EnrollmentStatus newStatus) {
        if (newStatus == null) throw new EnrollmentException.InvalidStatusTransitionException("New status cannot be null");

        switch (currentStatus) {
            case ACTIVE:
                if (newStatus == EnrollmentStatus.COMPLETED || newStatus == EnrollmentStatus.CANCELLED || newStatus == EnrollmentStatus.SUSPENDED) return;
                break;
            case SUSPENDED:
                if (newStatus == EnrollmentStatus.ACTIVE || newStatus == EnrollmentStatus.CANCELLED) return;
                break;
            case COMPLETED:
            case CANCELLED:
                throw new EnrollmentException.InvalidStatusTransitionException("Cannot change status from " + currentStatus + " to " + newStatus);
        }
        throw new EnrollmentException.InvalidStatusTransitionException("Invalid status transition from " + currentStatus + " to " + newStatus);
    }

    private void validatePaymentStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        if (newStatus == null) throw new EnrollmentException.InvalidPaymentStatusTransitionException("New payment status cannot be null");

        switch (currentStatus) {
            case PENDING:
                if (newStatus == PaymentStatus.PAID || newStatus == PaymentStatus.REFUNDED || newStatus == PaymentStatus.CANCELLED) return;
                break;
            case PAID:
                if (newStatus == PaymentStatus.REFUNDED) return;
                break;
            case REFUNDED:
            case CANCELLED:
                throw new EnrollmentException.InvalidPaymentStatusTransitionException("Cannot change payment status from " + currentStatus + " to " + newStatus);
        }
        throw new EnrollmentException.InvalidPaymentStatusTransitionException("Invalid payment status transition from " + currentStatus + " to " + newStatus);
    }

    // ========== Updaters ==========

    private void applyFullUpdate(Enrollment enrollment, EnrollmentRequestDto request) {
        enrollment.setCourseId(request.getCourseId());
        enrollment.setStudentId(request.getStudentId());
        enrollment.setEnrolledAt(request.getEnrolledAt());
        enrollment.setEnrollmentStatus(request.getEnrollmentStatus());
        enrollment.setPaymentStatus(request.getPaymentStatus());
    }

    private void applyPartialUpdate(Enrollment enrollment, EnrollmentRequestDto request) {
        if (request.getCourseId() != null) enrollment.setCourseId(request.getCourseId());
        if (request.getStudentId() != null) enrollment.setStudentId(request.getStudentId());
        if (request.getEnrolledAt() != null) enrollment.setEnrolledAt(request.getEnrolledAt());
        if (request.getEnrollmentStatus() != null) enrollment.setEnrollmentStatus(request.getEnrollmentStatus());
        if (request.getPaymentStatus() != null) enrollment.setPaymentStatus(request.getPaymentStatus());
    }
}