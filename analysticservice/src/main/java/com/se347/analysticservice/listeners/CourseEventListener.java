package com.se347.analysticservice.listeners;

import com.rabbitmq.client.Channel;
import com.se347.analysticservice.dtos.events.course.CourseCreatedEvent;
import com.se347.analysticservice.domains.services.overview.OverviewSynchronizationService;
import com.se347.analysticservice.entities.admin.revenue.InstructorRevenue;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.repositories.InstructorRevenueRepository;
import com.se347.analysticservice.services.admin.InstructorRevenueGenerationService;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CourseEventListener {
    
    private final PlatformOverviewService platformOverviewService;
    private final InstructorOverviewService instructorOverviewService;
    private final InstructorCourseStatsService instructorCourseStatsService;
    private final OverviewSynchronizationService overviewSynchronizationService;
    private final InstructorRevenueGenerationService instructorRevenueGenerationService;
    private final InstructorRevenueRepository instructorRevenueRepository;
    
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
            
            // Initialize InstructorRevenue for new instructors
            initializeInstructorRevenueIfNeeded(event.getInstructorId(), event.getOccurredAt().toLocalDate());
            
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
    
    // ==================== Instructor Revenue Initialization ====================
    
    /**
     * Initializes InstructorRevenue for new instructors.
     * Creates revenue records for DAILY, WEEKLY, and MONTHLY periods.
     * 
     * BUSINESS RULE:
     * - Only initializes if instructor has no existing InstructorRevenue records
     * - Creates records for the current period (day, week, month containing the event date)
     */
    private void initializeInstructorRevenueIfNeeded(UUID instructorId, LocalDate eventDate) {
        try {
            // Check if instructor already has any revenue records
            Optional<InstructorRevenue> existingRevenue = instructorRevenueRepository
                .findLatestByInstructorId(instructorId);
            
            if (existingRevenue.isPresent()) {
                log.debug("Instructor {} already has revenue records, skipping initialization", instructorId);
                return;
            }
            
            log.info("Initializing InstructorRevenue for new instructor: {}", instructorId);
            
            // Initialize DAILY revenue for the event date
            initializeDailyRevenue(instructorId, eventDate);
            
            // Initialize WEEKLY revenue for the week containing the event date
            initializeWeeklyRevenue(instructorId, eventDate);
            
            // Initialize MONTHLY revenue for the month containing the event date
            initializeMonthlyRevenue(instructorId, eventDate);
            
            log.info("Successfully initialized InstructorRevenue for instructor: {}", instructorId);
            
        } catch (Exception e) {
            // Log error but don't fail the entire event processing
            log.error("Failed to initialize InstructorRevenue for instructor: {}", instructorId, e);
        }
    }
    
    private void initializeDailyRevenue(UUID instructorId, LocalDate date) {
        try {
            instructorRevenueGenerationService.generateInstructorRevenue(
                instructorId,
                Period.DAILY,
                date,
                date
            );
            log.debug("Initialized DAILY revenue for instructor: {} on {}", instructorId, date);
        } catch (Exception e) {
            log.error("Failed to initialize DAILY revenue for instructor: {}", instructorId, e);
        }
    }
    
    private void initializeWeeklyRevenue(UUID instructorId, LocalDate date) {
        try {
            // Calculate start of week (Monday)
            LocalDate startOfWeek = date.minusDays(date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
            // Calculate end of week (Sunday)
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            
            instructorRevenueGenerationService.generateInstructorRevenue(
                instructorId,
                Period.WEEKLY,
                startOfWeek,
                endOfWeek
            );
            log.debug("Initialized WEEKLY revenue for instructor: {} for week {}-{}", 
                instructorId, startOfWeek, endOfWeek);
        } catch (Exception e) {
            log.error("Failed to initialize WEEKLY revenue for instructor: {}", instructorId, e);
        }
    }
    
    private void initializeMonthlyRevenue(UUID instructorId, LocalDate date) {
        try {
            // Calculate first day of month
            LocalDate firstDayOfMonth = date.withDayOfMonth(1);
            // Calculate last day of month
            LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());
            
            instructorRevenueGenerationService.generateInstructorRevenue(
                instructorId,
                Period.MONTHLY,
                firstDayOfMonth,
                lastDayOfMonth
            );
            log.debug("Initialized MONTHLY revenue for instructor: {} for month {}-{}", 
                instructorId, firstDayOfMonth, lastDayOfMonth);
        } catch (Exception e) {
            log.error("Failed to initialize MONTHLY revenue for instructor: {}", instructorId, e);
        }
    }
    
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

