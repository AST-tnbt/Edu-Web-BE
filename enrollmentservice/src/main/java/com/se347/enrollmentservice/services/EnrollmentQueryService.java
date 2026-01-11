package com.se347.enrollmentservice.services;

import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import java.util.List;
import java.util.UUID;

public interface EnrollmentQueryService {
    EnrollmentResponseDto getEnrollmentById(UUID enrollmentId, UUID userId);
    List<EnrollmentResponseDto> getEnrollmentsByStudentId(UUID studentId);
    boolean isEnrollmentExists(UUID courseId, UUID studentId);
    List<EnrollmentResponseDto> getEnrollmentsByCourseId(UUID courseId);
    boolean isEnrollmentEmpty(UUID courseId);
    EnrollmentResponseDto getEnrollmentByCourseIdAndStudentId(UUID courseId, UUID studentId);
    
    /**
     * Internal method for system/event processing - no authorization check
     * Used by RabbitMQ listeners and internal services
     */
    List<EnrollmentResponseDto> getEnrollmentsByCourseIdInternal(UUID courseId);
}
