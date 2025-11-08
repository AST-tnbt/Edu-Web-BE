package com.se347.enrollmentservice.clients.impl;

import com.se347.enrollmentservice.clients.CourseServiceClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.UUID;
import java.time.Duration;

public class CourseServiceClientImpl implements CourseServiceClient{
    private final WebClient courseServiceClient;
    private static final Logger logger = LoggerFactory.getLogger(CourseServiceClientImpl.class);

    public CourseServiceClientImpl(WebClient courseServiceClient) {
        this.courseServiceClient = courseServiceClient;
    }

    public Integer getTotalLessonsByCourseId(UUID courseId) {
        try {
            return courseServiceClient.get()
                .uri("/api/courses/{courseId}/total-lessons", courseId)
                .retrieve()
                .bodyToMono(Integer.class)
                .timeout(Duration.ofSeconds(3))
                .blockOptional()
                .orElse(0);
        } catch (Exception e) {
            logger.warn("Failed to get totalLessons from CourseService, using default 0", e);
            throw new RuntimeException("Failed to get totalLessons from CourseService", e);
        }
    }
}
