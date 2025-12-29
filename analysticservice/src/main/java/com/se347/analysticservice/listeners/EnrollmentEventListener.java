package com.se347.analysticservice.listeners;

import com.rabbitmq.client.Channel;
import com.se347.analysticservice.dtos.events.progress.CourseCompletedEvent;
import com.se347.analysticservice.dtos.events.progress.CourseProgressUpdatedEvent;
import com.se347.analysticservice.dtos.events.enrollment.EnrollmentCreatedEvent;
import com.se347.analysticservice.services.PlatformOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class EnrollmentEventListener {
    
    private final PlatformOverviewService platformOverviewService;
    
    /**
     * Handles EnrollmentCreatedEvent.
     * Updates enrollment count in platform metrics and instructor stats.
     */
    @RabbitListener(
        queues = "${app.rabbitmq.queue.enrollment-created}",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleEnrollmentCreated(
        EnrollmentCreatedEvent event,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {
        try {
            // Validate event
            validateEnrollmentCreatedEvent(event);
            
            // Delegate to application service
            platformOverviewService.recordEnrollment(
                event.getEnrollmentId(),
                event.getStudentId(),
                event.getCourseId(),
                event.getInstructorId(),
                event.getEnrolledAt().toLocalDate()
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

    /**
     * Handles CourseCompletedEvent.
     * Updates completion rate metrics when a student completes a course.
     */
    @RabbitListener(
        queues = "${app.rabbitmq.queue.course-completed}",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleCourseCompleted(
        CourseCompletedEvent event,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {
        try {
            // Validate event
            validateCourseCompletedEvent(event);
            
            // Delegate to application service
            platformOverviewService.recordCourseCompletion(
                event.getStudentId(),
                event.getCourseId(),
                event.getInstructorId(),
                event.getEnrollmentId(),
                event.getCompletedAt().toLocalDate()
            );
            
            // Acknowledge success
            acknowledgeMessage(channel, deliveryTag);
        } catch (IllegalArgumentException e) {
            rejectMessage(channel, deliveryTag, false);
            
        } catch (Exception e) {
            rejectMessage(channel, deliveryTag, true);
        }
    }
    
    /**
     * Handles CourseProgressUpdatedEvent.
     * Updates average completion rate metrics for platform analytics.
     */
    @RabbitListener(
        queues = "${app.rabbitmq.queue.course-progress-updated}",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleCourseProgressUpdated(
        CourseProgressUpdatedEvent event,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {
        try {
            // Validate event
            validateCourseProgressUpdatedEvent(event);
            
            // Delegate to application service
            platformOverviewService.recordProgressUpdate(
                event.getStudentId(),
                event.getCourseId(),
                event.getInstructorId(),
                event.getCurrentCompletionRate(),
                event.getUpdatedAt().toLocalDate()
            );
            
            // Acknowledge success
            acknowledgeMessage(channel, deliveryTag);
        } catch (IllegalArgumentException e) {
            rejectMessage(channel, deliveryTag, false);
            
        } catch (Exception e) {
            rejectMessage(channel, deliveryTag, true);
        }
    }
    
    // ==================== Validation Methods ====================
    
    private void validateEnrollmentCreatedEvent(EnrollmentCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("EnrollmentCreatedEvent cannot be null");
        }
        if (event.getEnrollmentId() == null) {
            throw new IllegalArgumentException("EnrollmentId cannot be null in EnrollmentCreatedEvent");
        }
        if (event.getStudentId() == null) {
            throw new IllegalArgumentException("StudentId cannot be null in EnrollmentCreatedEvent");
        }
        if (event.getCourseId() == null) {
            throw new IllegalArgumentException("CourseId cannot be null in EnrollmentCreatedEvent");
        }
        if (event.getInstructorId() == null) {
            throw new IllegalArgumentException("InstructorId cannot be null in EnrollmentCreatedEvent");
        }
        if (event.getEnrolledAt() == null) {
            throw new IllegalArgumentException("EnrolledAt cannot be null in EnrollmentCreatedEvent");
        }
    }
    
    private void validateCourseCompletedEvent(CourseCompletedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("CourseCompletedEvent cannot be null");
        }
        if (event.getStudentId() == null) {
            throw new IllegalArgumentException("StudentId cannot be null in CourseCompletedEvent");
        }
        if (event.getCourseId() == null) {
            throw new IllegalArgumentException("CourseId cannot be null in CourseCompletedEvent");
        }
        if (event.getInstructorId() == null) {
            throw new IllegalArgumentException("InstructorId cannot be null in CourseCompletedEvent");
        }
        if (event.getCompletedAt() == null) {
            throw new IllegalArgumentException("CompletedAt cannot be null in CourseCompletedEvent");
        }
    }
    
    private void validateCourseProgressUpdatedEvent(CourseProgressUpdatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("CourseProgressUpdatedEvent cannot be null");
        }
        if (event.getStudentId() == null) {
            throw new IllegalArgumentException("StudentId cannot be null in CourseProgressUpdatedEvent");
        }
        if (event.getCourseId() == null) {
            throw new IllegalArgumentException("CourseId cannot be null in CourseProgressUpdatedEvent");
        }
        if (event.getCurrentCompletionRate() == null) {
            throw new IllegalArgumentException("CurrentCompletionRate cannot be null in CourseProgressUpdatedEvent");
        }
        if (event.getUpdatedAt() == null) {
            throw new IllegalArgumentException("UpdatedAt cannot be null in CourseProgressUpdatedEvent");
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

