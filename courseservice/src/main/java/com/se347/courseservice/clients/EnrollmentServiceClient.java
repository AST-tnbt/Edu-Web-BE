package com.se347.courseservice.clients;

import com.se347.courseservice.dtos.events.EnrollmentResponseEventDto;
import java.util.List;
import java.util.UUID;

public interface EnrollmentServiceClient {
    List<EnrollmentResponseEventDto> getEnrollmentsbyCourseId(UUID courseId);
}
