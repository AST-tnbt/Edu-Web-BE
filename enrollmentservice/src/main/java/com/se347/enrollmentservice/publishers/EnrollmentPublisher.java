package com.se347.enrollmentservice.publishers;

import com.se347.enrollmentservice.domains.events.EnrollmentCreatedEvent;
import com.se347.enrollmentservice.domains.events.EnrollmentCompletedEvent;
import com.se347.enrollmentservice.domains.events.UpdateOverallProgressEvent;

public interface EnrollmentPublisher {
    void publishEnrollmentCreatedEvent(EnrollmentCreatedEvent event);
    void publishEnrollmentCompletedEvent(EnrollmentCompletedEvent event);
    void publishUpdateOverallProgressEvent(UpdateOverallProgressEvent event);
}
