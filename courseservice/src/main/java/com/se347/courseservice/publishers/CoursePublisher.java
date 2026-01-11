package com.se347.courseservice.publishers;

import com.se347.courseservice.domains.events.CourseLessonChangedEvent;
import com.se347.courseservice.domains.events.CourseCreatedEvent;

public interface CoursePublisher {
    void publishSetTotalLessonsEvent(CourseLessonChangedEvent event);
    void publishCourseCreatedEvent(CourseCreatedEvent event);
}
