package com.se347.enrollmentservice.publishers;

import com.se347.enrollmentservice.domains.events.EnrollmentCreatedEvent;

public interface EnrollmentPublisher {
    void publishEnrollmentCreatedEvent(EnrollmentCreatedEvent event);
}
