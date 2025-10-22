package com.se347.enrollmentservice.services;

import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;

import java.util.List;
import java.util.UUID;

public interface EnrollmentService {
    EnrollmentResponseDto createEnrollment(EnrollmentRequestDto request);
    EnrollmentResponseDto getEnrollmentById(UUID enrollmentId);
    EnrollmentResponseDto updateEnrollment(UUID enrollmentId, EnrollmentRequestDto request);
    // void deleteEnrollment(UUID enrollmentId);
    List<EnrollmentResponseDto> getEnrollmentsByStudentId(UUID studentId);
    List<EnrollmentResponseDto> getEnrollmentsByCourseId(UUID courseId);
    List<EnrollmentResponseDto> getEnrollmentsByCourseIdAndStudentId(UUID courseId, UUID studentId);
    boolean isEnrollmentExists(UUID enrollmentId);
    List<EnrollmentResponseDto> getAllEnrollments();
}
