package com.se347.courseservice.listeners;

import com.se347.courseservice.domains.events.CourseCreatedEvent;
import com.se347.courseservice.domains.events.CourseUpdatedEvent;
import com.se347.courseservice.domains.events.CourseLessonChangedEvent;

public interface CourseEventListener {
    void handleCourseCreatedEvent(CourseCreatedEvent courseCreatedEvent);
    void handleCourseUpdatedEvent(CourseUpdatedEvent courseUpdatedEvent);
    void handleCourseLessonChangedEvent(CourseLessonChangedEvent courseLessonChangedEvent);
}
