package com.se347.analysticservice.listeners;

import com.rabbitmq.client.Channel;
import com.se347.analysticservice.dtos.events.course.CourseCreatedEvent;
import com.se347.analysticservice.domains.services.overview.OverviewSynchronizationService;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.services.admin.PlatformOverviewService;
import com.se347.analysticservice.services.instructor.InstructorCourseStatsService;
import com.se347.analysticservice.services.instructor.InstructorOverviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CourseEventListener {
    
    private final PlatformOverviewService platformOverviewService;
    private final InstructorOverviewService instructorOverviewService;
    private final InstructorCourseStatsService instructorCourseStatsService;
    private final OverviewSynchronizationService overviewSynchronizationService;
    
    /**
     * Handles CourseCreatedEvent from Course Service.
     * Updates course creation metrics and instructor stats.
     */
    @RabbitListener(
        queues = "${app.rabbitmq.queue.course-created}",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleCourseCreated(
        CourseCreatedEvent event,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {
        try {
            // Validate event
            validateCourseCreatedEvent(event);
            
            platformOverviewService.recordCourseCreation(
                event.getCourseId(),
                event.getInstructorId(),
                event.getOccurredAt().toLocalDate()
            );
            
            instructorOverviewService.recordCourse(
                event.getInstructorId(), 
                event.getCourseId()
            );
            
            instructorCourseStatsService.ensureCourseStatsExists(
                event.getInstructorId(), 
                event.getCourseId()
            );
            
            // Synchronize overview entities after source entities are updated
            // DDD PATTERN: Domain Service handles cross-aggregate synchronization
            overviewSynchronizationService.synchronizeInstructorOverview(event.getInstructorId());
            overviewSynchronizationService.synchronizeCurrentPeriodOverview(Period.DAILY);
            
            // Acknowledge success
            acknowledgeMessage(channel, deliveryTag);
        } catch (IllegalArgumentException e) {
            // Validation errors - reject without requeue (poison message)
            rejectMessage(channel, deliveryTag, false);
            
        } catch (Exception e) {
            // Business/infrastructure errors - reject WITH requeue (transient error)
            rejectMessage(channel, deliveryTag, true);
        }
    }
    
    // @RabbitListener(
    //     queues = "${app.rabbitmq.queue.course-published}",
    //     containerFactory = "rabbitListenerContainerFactory"
    // )
    // public void handleCoursePublished(
    //     CoursePublishedEvent event,
    //     Channel channel,
    //     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    // ) {
    //     try {
    //         // Validate event
    //         validateCoursePublishedEvent(event);
            
    //         // Delegate to application service
    //         platformOverviewService.recordCoursePublication(
    //             event.getCourseId(),
    //             event.getInstructorId(),
    //             event.getPublishedAt().toLocalDate()
    //         );
            
    //         // Acknowledge success
    //         acknowledgeMessage(channel, deliveryTag);
    //     } catch (IllegalArgumentException e) {
    //         rejectMessage(channel, deliveryTag, false);
            
    //     } catch (Exception e) {
    //         rejectMessage(channel, deliveryTag, true);
    //     }
    // }
    
    // ==================== Validation Methods ====================
    
    private void validateCourseCreatedEvent(CourseCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("CourseCreatedEvent cannot be null");
        }
        if (event.getCourseId() == null) {
            throw new IllegalArgumentException("CourseId cannot be null in CourseCreatedEvent");
        }
        if (event.getInstructorId() == null) {
            throw new IllegalArgumentException("InstructorId cannot be null in CourseCreatedEvent");
        }
        if (event.getOccurredAt() == null) {
            throw new IllegalArgumentException("OccurredAt cannot be null in CourseCreatedEvent");
        }
    }
    
    
    // ==================== Message Acknowledgment Methods ====================
    
    private void acknowledgeMessage(Channel channel, long deliveryTag) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.basicAck(deliveryTag, false);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to acknowledge message", e);
        }
    }
    
    private void rejectMessage(Channel channel, long deliveryTag, boolean requeue) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.basicNack(deliveryTag, false, requeue);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to reject message", e);
        }
    }
}

