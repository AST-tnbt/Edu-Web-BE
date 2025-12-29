package com.se347.enrollmentservice.services;

import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;

import java.util.UUID;

public interface CourseProgressQueryService {
    CourseProgressResponseDto getCourseProgressById(UUID courseProgressId);
    CourseProgressResponseDto getCourseProgressByEnrollmentId(UUID enrollmentId);
}
