package com.se347.enrollmentservice.clients;

import java.util.UUID;

public interface CourseServiceClient {
    Integer getTotalLessonsByCourseId(UUID courseId);
}