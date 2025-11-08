package com.se347.enrollmentservice.listeners.impl;

import com.se347.enrollmentservice.dtos.events.TotalLessonsEventDto;
import com.se347.enrollmentservice.listeners.CourseTotalLessonsListener;
import com.se347.enrollmentservice.services.EnrollmentService;
import com.se347.enrollmentservice.services.CourseProgressService;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@Service
public class CourseTotalLessonsListenerImpl implements CourseTotalLessonsListener {

    private static final Logger logger = LoggerFactory.getLogger(CourseTotalLessonsListenerImpl.class);
    
    private final EnrollmentService enrollmentService;
    private final CourseProgressService courseProgressService;

    @RabbitListener(queues = "${app.rabbitmq.queue.set-total-lessons}", containerFactory = "rabbitListenerContainerFactory")
    public void handleSetTotalLessonsEvent(TotalLessonsEventDto event,
                                          Channel channel,
                                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        long startTime = System.currentTimeMillis();
        UUID courseId = event.getCourseId();
        Integer newTotalLessons = event.getTotalLessons();
        
        try {
            // Validate event
            validateEvent(event);
            
            // Get all enrollments for this course
            List<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId);
            
            if (enrollments.isEmpty()) {
                logger.info("✅ [COURSE] No enrollments found for course: {}, skipping update", courseId);
                acknowledgeMessage(channel, deliveryTag, "No enrollments to update");
                return;
            }
            
            // Update totalLessons for all CourseProgress of this course
            int updatedCount = 0;
            int failedCount = 0;
            
            for (EnrollmentResponseDto enrollment : enrollments) {
                try {
                    // Get CourseProgress by enrollmentId
                    CourseProgressResponseDto courseProgress = courseProgressService.getCourseProgressByEnrollmentId(enrollment.getEnrollmentId());
                    
                    // Update totalLessons (this will recalculate progress automatically)
                    courseProgressService.setTotalLessons(courseProgress.getCourseProgressId(), newTotalLessons);
                    
                    updatedCount++;
                } catch (Exception e) {
                    failedCount++;
                    logger.error("❌ [COURSE] Failed to update CourseProgress for enrollment: {} - Error: {}", 
                        enrollment.getEnrollmentId(), e.getMessage(), e);
                    // Continue with other enrollments
                }
            }
            
            // Acknowledge message after processing
            acknowledgeMessage(channel, deliveryTag, "Updated " + updatedCount + " CourseProgress records");
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Log warning if all updates failed
            if (updatedCount == 0 && failedCount > 0) {
                logger.warn("⚠️ [COURSE] All CourseProgress updates failed for course: {} - Total enrollments: {}, Failed: {}", 
                    courseId, enrollments.size(), failedCount);
            }
            
            logger.info("✅ [COURSE] Successfully processed SetTotalLessonsEvent - CourseId: {}, NewTotalLessons: {}, Updated: {}, Failed: {}, ProcessingTime: {}ms", 
                courseId, newTotalLessons, updatedCount, failedCount, processingTime);
            
        } catch (IllegalArgumentException e) {
            logError(courseId, newTotalLessons, startTime, deliveryTag, e, "Validation error");
            rejectMessage(channel, deliveryTag, false);
        } catch (Exception e) {
            logError(courseId, newTotalLessons, startTime, deliveryTag, e, "Processing error");
            rejectMessage(channel, deliveryTag, false);
        }
    }
    
    /**
     * Validates the set total lessons event
     */
    private void validateEvent(TotalLessonsEventDto event) {
        if (event == null) {
            throw new IllegalArgumentException("TotalLessonsEventDto cannot be null");
        }
        if (event.getCourseId() == null) {
            throw new IllegalArgumentException("CourseId cannot be null in TotalLessonsEventDto");
        }
        if (event.getTotalLessons() == null || event.getTotalLessons() < 0) {
            throw new IllegalArgumentException("TotalLessons must be non-negative in TotalLessonsEventDto");
        }
    }
    
    /**
     * Acknowledges the message
     */
    private void acknowledgeMessage(Channel channel, long deliveryTag, String reason) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.basicAck(deliveryTag, false);
                logger.debug("✅ [COURSE] Message acknowledged - DeliveryTag: {}, Reason: {}", deliveryTag, reason);
            } else {
                logger.warn("⚠️ [COURSE] Channel is closed, cannot acknowledge message - DeliveryTag: {}", deliveryTag);
            }
        } catch (IOException e) {
            logger.error("❌ [COURSE] Failed to acknowledge message - DeliveryTag: {}, Error: {}", 
                deliveryTag, e.getMessage(), e);
            throw new RuntimeException("Failed to acknowledge message", e);
        }
    }
    
    /**
     * Rejects the message
     */
    private void rejectMessage(Channel channel, long deliveryTag, boolean requeue) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.basicNack(deliveryTag, false, requeue);
                logger.warn("⚠️ [COURSE] Message rejected - DeliveryTag: {}, Requeue: {}", deliveryTag, requeue);
            } else {
                logger.warn("⚠️ [COURSE] Channel is closed, cannot reject message - DeliveryTag: {}", deliveryTag);
            }
        } catch (IOException e) {
            logger.error("❌ [COURSE] Failed to reject message - DeliveryTag: {}, Error: {}", 
                deliveryTag, e.getMessage(), e);
            throw new RuntimeException("Failed to reject message", e);
        }
    }
    
    /**
     * Logs error during processing
     */
    private void logError(UUID courseId, Integer totalLessons, long startTime, long deliveryTag, Exception e, String errorType) {
        long processingTime = System.currentTimeMillis() - startTime;
        logger.error("❌ [COURSE] Failed to process SetTotalLessonsEvent - CourseId: {}, TotalLessons: {}, ErrorType: {}, ProcessingTime: {}ms, DeliveryTag: {}, Error: {}", 
            courseId, totalLessons, processingTime, deliveryTag, e.getMessage(), e);
    }
}

