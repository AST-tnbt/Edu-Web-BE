package com.se347.enrollmentservice.services;

import java.util.UUID;
import java.util.List;
import com.se347.enrollmentservice.dtos.MyCourseProgressDto;
import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;

public interface EnrollmentDomainService {
    EnrollmentResponseDto createEnrollment(EnrollmentRequestDto request);
    List<MyCourseProgressDto> getMyCourseProgress(UUID studentId);
    void ensureEnrollmentAccessible(UUID enrollmentId);
    void syncCourseProgressOnLessonChange(UUID enrollmentId, boolean increment);
}
