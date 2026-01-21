package com.se347.analysticservice.listeners;

import com.rabbitmq.client.Channel;

import com.se347.analysticservice.dtos.events.enrollment.EnrollmentCreatedEvent;
import com.se347.analysticservice.dtos.events.enrollment.EnrollmentCompletedEvent;
import com.se347.analysticservice.dtos.events.enrollment.UpdateOverallProgressEvent;
import com.se347.analysticservice.domains.services.overview.OverviewSynchronizationService;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.services.admin.PlatformOverviewService;
import com.se347.analysticservice.services.instructor.InstructorCourseStatsService;
import com.se347.analysticservice.services.instructor.InstructorDailyStatsService;
import com.se347.analysticservice.services.instructor.InstructorOverviewService;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class EnrollmentEventListener {
    
    private final PlatformOverviewService platformOverviewService;
    private final InstructorOverviewService instructorOverviewService;
    private final InstructorCourseStatsService instructorCourseStatsService;
    private final InstructorDailyStatsService instructorDailyStatsService;
    private final OverviewSynchronizationService overviewSynchronizationService;
    
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
            
            var enrolledDate = event.getEnrolledAt().toLocalDate();
            
            platformOverviewService.recordEnrollment(
                event.getEnrollmentId(),
                event.getStudentId(),
                event.getCourseId(),
                event.getInstructorId(),
                enrolledDate
            );
            
            instructorOverviewService.recordEnrollment(
                event.getInstructorId(), 
                Count.one()
            );

            instructorCourseStatsService.recordEnrollment(
                event.getInstructorId(), 
                event.getCourseId(), 
                Count.one()
            );

            instructorDailyStatsService.recordEnrollment(
                event.getInstructorId(), 
                enrolledDate
            );

            instructorDailyStatsService.recordActiveStudent(
                event.getInstructorId(), 
                enrolledDate
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

    /**
     * Handles EnrollmentCompletedEvent.
     * Updates completion rate metrics when a student completes a course.
     */
    @RabbitListener(
        queues = "${app.rabbitmq.queue.enrollment-completed}",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleEnrollmentCompleted(
        EnrollmentCompletedEvent event,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {
        try {
            // Validate event
            validateEnrollmentCompletedEvent(event);
            
            LocalDate completedDate = event.getOccurredAt().toLocalDate();
            
            platformOverviewService.recordEnrollmentCompletion(
                event.getStudentId(),
                event.getCourseId(),
                event.getInstructorId(),
                event.getEnrollmentId(),
                completedDate
            );
            
            instructorDailyStatsService.recordCourseCompletion(
                event.getInstructorId(),
                completedDate
            );

            instructorDailyStatsService.recordActiveStudent(
                event.getInstructorId(), 
                completedDate
            );
            
            // Synchronize overview entities after source entities are updated
            overviewSynchronizationService.synchronizeInstructorOverview(event.getInstructorId());
            overviewSynchronizationService.synchronizeCurrentPeriodOverview(Period.DAILY);
            
            // Acknowledge success
            acknowledgeMessage(channel, deliveryTag);
        } catch (IllegalArgumentException e) {
            rejectMessage(channel, deliveryTag, false);
            
        } catch (Exception e) {
            rejectMessage(channel, deliveryTag, true);
        }
    }

    @RabbitListener(
        queues = "${app.rabbitmq.queue.update-overall-progress}",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleUpdateOverallProgress(
        UpdateOverallProgressEvent event,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {
        try {
            // Validate event
            validateUpdateOverallProgressEvent(event);
            
            instructorCourseStatsService.updateOverallProgress(
                event.getInstructorId(), 
                event.getCourseId(), 
                event.getEnrollmentId(), 
                event.getNewOverallProgress()
            );
            
            // Synchronize overview entities after source entities are updated
            // DDD PATTERN: Domain Service handles cross-aggregate synchronization
            overviewSynchronizationService.synchronizeInstructorOverview(event.getInstructorId());
            
            // Acknowledge success
            acknowledgeMessage(channel, deliveryTag);
        } catch (IllegalArgumentException e) {
            rejectMessage(channel, deliveryTag, false);
            
        } catch (Exception e) {
            rejectMessage(channel, deliveryTag, true);
        }
    }

    // ==================== Validation Methods ====================

    private void validateUpdateOverallProgressEvent(UpdateOverallProgressEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("UpdateOverallProgressEvent cannot be null");
        }
        if (event.getEnrollmentId() == null) {
            throw new IllegalArgumentException("EnrollmentId cannot be null in UpdateOverallProgressEvent");
        }
        if (event.getCourseId() == null) {
            throw new IllegalArgumentException("CourseId cannot be null in UpdateOverallProgressEvent");
        }
        if (event.getStudentId() == null) {
            throw new IllegalArgumentException("StudentId cannot be null in UpdateOverallProgressEvent");
        }
        if (event.getInstructorId() == null) {
            throw new IllegalArgumentException("InstructorId cannot be null in UpdateOverallProgressEvent");
        }
        if (event.getUpdatedAt() == null) {
            throw new IllegalArgumentException("UpdatedAt cannot be null in UpdateOverallProgressEvent");
        }
        double progress = event.getNewOverallProgress();
        if (progress < 0.0 || progress > 100.0) {
            throw new IllegalArgumentException("newOverallProgress must be between 0 and 100 in UpdateOverallProgressEvent");
        }
    }

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
    
    private void validateEnrollmentCompletedEvent(EnrollmentCompletedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("EnrollmentCompletedEvent cannot be null");
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
        if (event.getOccurredAt() == null) {
            throw new IllegalArgumentException("OccurredAt cannot be null in EnrollmentCompletedEvent");
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

