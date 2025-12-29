package com.se347.courseservice.listeners.impl;

import com.se347.courseservice.listeners.CourseEventListener;
import com.se347.courseservice.domains.events.CourseCreatedEvent;
import com.se347.courseservice.domains.events.CourseUpdatedEvent;
import com.se347.courseservice.domains.events.CourseLessonChangedEvent;
import com.se347.courseservice.publishers.CoursePublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseEventListenerImpl implements CourseEventListener {
    
    private final CoursePublisher coursePublisher;

    @Override
    public void handleCourseCreatedEvent(CourseCreatedEvent courseCreatedEvent) {
    }
    
    @Override
    public void handleCourseUpdatedEvent(CourseUpdatedEvent courseUpdatedEvent) {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCourseLessonChangedEvent(CourseLessonChangedEvent event) {
        coursePublisher.publishSetTotalLessonsEvent(event);
    }
}
