package com.se347.enrollmentservice.listeners.impl;

import com.se347.enrollmentservice.listeners.EnrollmentEventListener;
import com.se347.enrollmentservice.domains.events.EnrollmentCreatedEvent;
import com.se347.enrollmentservice.publishers.EnrollmentPublisher;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EnrollmentEventListenerImpl implements EnrollmentEventListener {

    private final EnrollmentPublisher enrollmentPublisher;

    @Override
    public void handleEnrollmentCreatedEvent(EnrollmentCreatedEvent event) {
        enrollmentPublisher.publishEnrollmentCreatedEvent(event);
    }
}
