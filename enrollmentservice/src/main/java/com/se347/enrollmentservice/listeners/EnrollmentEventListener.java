package com.se347.enrollmentservice.listeners;

import com.se347.enrollmentservice.domains.events.EnrollmentCreatedEvent;

public interface EnrollmentEventListener {
    void handleEnrollmentCreatedEvent(EnrollmentCreatedEvent event);
}
