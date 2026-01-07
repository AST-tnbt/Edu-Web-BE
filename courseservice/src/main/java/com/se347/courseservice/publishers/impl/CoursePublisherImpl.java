package com.se347.courseservice.publishers.impl;

import com.se347.courseservice.publishers.CoursePublisher;
import com.se347.courseservice.domains.events.CourseLessonChangedEvent;
import com.se347.courseservice.domains.events.CourseCreatedEvent;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@RequiredArgsConstructor
@Service
public class CoursePublisherImpl implements CoursePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final Logger logger = LoggerFactory.getLogger(CoursePublisherImpl.class);

    @Value("${app.rabbitmq.exchange.enrollment-course}")
    private String enrollmentCourseExchangeName;

    @Value("${app.rabbitmq.exchange.course}")
    private String courseExchangeName;

    @Value("${app.rabbitmq.routing-key.course-created}")
    private String courseCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.set-total-lessons}")
    private String setTotalLessonsRoutingKey;

    @Override
    public void publishSetTotalLessonsEvent(CourseLessonChangedEvent event) {
        logger.info("[Course -> Enrollment] Publishing set total lessons event: {}", event);
        rabbitTemplate.convertAndSend(enrollmentCourseExchangeName, 
                                    setTotalLessonsRoutingKey, 
                                    event);
    }

    @Override
    public void publishCourseCreatedEvent(CourseCreatedEvent event) {
        logger.info("[Course -> Analytics] Publishing course created event: {}", event);
        rabbitTemplate.convertAndSend(courseExchangeName, 
                                    courseCreatedRoutingKey, 
                                    event);
    }
}
