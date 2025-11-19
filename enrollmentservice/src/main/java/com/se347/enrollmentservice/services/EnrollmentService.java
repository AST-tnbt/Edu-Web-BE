package com.se347.enrollmentservice.services;

import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.enums.PaymentStatus;
import com.se347.enrollmentservice.enums.EnrollmentStatus;
import com.se347.enrollmentservice.entities.Enrollment;

import java.util.List;
import java.util.UUID;

public interface EnrollmentService {
    EnrollmentResponseDto createEnrollment(EnrollmentRequestDto request);
    EnrollmentResponseDto getEnrollmentById(UUID enrollmentId);
    EnrollmentResponseDto updateEnrollment(UUID enrollmentId, EnrollmentRequestDto request);
    Enrollment toEnrollment(UUID enrollmentId);
    List<EnrollmentResponseDto> getEnrollmentsByStudentId(UUID studentId);
    List<EnrollmentResponseDto> getEnrollmentsByCourseId(UUID courseId);
    List<EnrollmentResponseDto> getEnrollmentsByCourseIdAndStudentId(UUID courseId, UUID studentId);
    List<EnrollmentResponseDto> getAllEnrollments();
    boolean isEnrollmentExists(UUID enrollmentId);
    EnrollmentResponseDto updateEnrollmentStatus(UUID enrollmentId, EnrollmentStatus newStatus);
    EnrollmentResponseDto updatePaymentStatus(UUID enrollmentId, PaymentStatus newStatus);
    EnrollmentResponseDto updateStatuses(UUID enrollmentId, EnrollmentStatus enrollmentStatus, PaymentStatus paymentStatus);
}
