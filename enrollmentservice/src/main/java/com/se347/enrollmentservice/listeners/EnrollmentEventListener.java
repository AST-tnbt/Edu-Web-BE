package com.se347.enrollmentservice.listeners;

import com.se347.enrollmentservice.domains.events.EnrollmentCreatedEvent;
import com.se347.enrollmentservice.domains.events.EnrollmentCompletedEvent;
import com.se347.enrollmentservice.domains.events.UpdateOverallProgressEvent;

public interface EnrollmentEventListener {
    void handleEnrollmentCreatedEvent(EnrollmentCreatedEvent event);
    
    void handleEnrollmentCompletedEvent(EnrollmentCompletedEvent event);

    void handleUpdateOverallProgressEvent(UpdateOverallProgressEvent event);
}
