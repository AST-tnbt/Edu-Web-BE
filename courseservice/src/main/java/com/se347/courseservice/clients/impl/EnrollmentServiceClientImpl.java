package com.se347.courseservice.clients.impl;

import com.se347.courseservice.dtos.events.EnrollmentResponseEventDto;

import java.util.List;
import com.se347.courseservice.clients.EnrollmentServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;

@RequiredArgsConstructor
@Service
public class EnrollmentServiceClientImpl implements EnrollmentServiceClient {    
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentServiceClientImpl.class);
    private final WebClient enrollmentServiceClient;

    @Override
    public List<EnrollmentResponseEventDto> getEnrollmentsbyCourseId(UUID courseId) {
        try {
        return enrollmentServiceClient.get()
            .uri("/api/enrollments/course/{courseId}", courseId)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<EnrollmentResponseEventDto>>() {})
            .blockOptional()
            .orElseThrow(() -> new RuntimeException("Failed to get enrollments for course with ID: " + courseId));
        } catch (Exception e) {
            logger.warn("Failed to get enrollments for course with ID: " + courseId, e);
            throw new RuntimeException("Failed to get enrollments for course with ID: " + courseId, e);
        }
    }
}
