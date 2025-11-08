package com.se347.courseservice.publishers;

import com.se347.courseservice.dtos.events.setTotalLessonsEventDto;

public interface CoursePublisher {
    void publishSetTotalLessonsEvent(setTotalLessonsEventDto event);
}
