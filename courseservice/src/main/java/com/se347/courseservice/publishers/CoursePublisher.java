package com.se347.courseservice.publishers;

import com.se347.courseservice.domains.events.CourseLessonChangedEvent;

public interface CoursePublisher {
    void publishSetTotalLessonsEvent(CourseLessonChangedEvent event);
}
