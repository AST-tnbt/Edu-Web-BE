package com.se347.analysticservice.listeners;

import com.rabbitmq.client.Channel;
import com.se347.analysticservice.dtos.events.course.CourseCreatedEvent;
import com.se347.analysticservice.dtos.events.course.CoursePublishedEvent;
import com.se347.analysticservice.services.InstructorAnalyticsService;
import com.se347.analysticservice.services.PlatformOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CourseEventListener {
    
    private final PlatformOverviewService platformOverviewService;
    private final InstructorAnalyticsService instructorAnalyticsService;
    
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
            
            // Delegate to application services
            platformOverviewService.recordCourseCreation(
                event.getCourseId(),
                event.getInstructorId(),
                event.getOccurredAt().toLocalDate()
            );
            
            instructorAnalyticsService.recordCourseAddedToInstructor(
                event.getInstructorId(),
                event.getCourseId()
            );
            
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
    
    private void validateCoursePublishedEvent(CoursePublishedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("CoursePublishedEvent cannot be null");
        }
        if (event.getCourseId() == null) {
            throw new IllegalArgumentException("CourseId cannot be null in CoursePublishedEvent");
        }
        if (event.getInstructorId() == null) {
            throw new IllegalArgumentException("InstructorId cannot be null in CoursePublishedEvent");
        }
        if (event.getPublishedAt() == null) {
            throw new IllegalArgumentException("PublishedAt cannot be null in CoursePublishedEvent");
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

