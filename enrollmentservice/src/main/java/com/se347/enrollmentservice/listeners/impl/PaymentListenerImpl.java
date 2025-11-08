package com.se347.enrollmentservice.listeners.impl;

import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.events.PaymentCompletedEventDto;
import com.se347.enrollmentservice.listeners.PaymentListener;
import com.se347.enrollmentservice.services.EnrollmentService;
import com.se347.enrollmentservice.enums.EnrollmentStatus;
import com.se347.enrollmentservice.enums.PaymentStatus;
import com.se347.enrollmentservice.clients.CourseServiceClient;
import com.se347.enrollmentservice.entities.CourseProgress;
import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.services.CourseProgressService;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@Service
public class PaymentListenerImpl implements PaymentListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentListenerImpl.class);
    private final EnrollmentService enrollmentService;
    private final CourseServiceClient courseServiceClient;
    private final CourseProgressService courseProgressService;

    @RabbitListener(queues = "${app.rabbitmq.queue.payment.completed}", containerFactory = "rabbitListenerContainerFactory")
    public void handlePaymentCompletedEvent(PaymentCompletedEventDto paymentCompletedEventDto,
                                            Channel channel,
                                            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        long startTime = System.currentTimeMillis();
        UUID courseId = paymentCompletedEventDto.getCourseId();
        UUID userId = paymentCompletedEventDto.getUserId();
        
        try {
            // Validate input
            validatePaymentEvent(paymentCompletedEventDto);
            
            // Check if enrollment already exists (idempotency check)
            boolean enrollmentExists = !enrollmentService
                .getEnrollmentsByCourseIdAndStudentId(courseId, userId)
                .isEmpty();
            
            if (enrollmentExists) {
                logger.info("✅ [PAYMENT] Enrollment already exists (idempotent) - CourseId: {}, UserId: {}", 
                    courseId, userId);
                // Acknowledge message even if enrollment exists (idempotent operation)
                acknowledgeMessage(channel, deliveryTag, "Enrollment already exists");
                logSuccess(courseId, userId, startTime, deliveryTag, "Idempotent - enrollment exists");
                return;
            }

            // Create new enrollment with proper statuses
            UUID enrollmentId = UUID.randomUUID();
            EnrollmentRequestDto enrollmentRequest = buildEnrollmentRequest(enrollmentId, courseId, userId);
            enrollmentService.createEnrollment(enrollmentRequest);
            
            // Acknowledge message after successful enrollment creation
            acknowledgeMessage(channel, deliveryTag, "Enrollment created successfully");
            logSuccess(courseId, userId, startTime, deliveryTag, "New enrollment created");
            
            
            // Get total lessons from CourseService
            Integer totalLessons = courseServiceClient.getTotalLessonsByCourseId(courseId);
            
            // Create course progress
            CourseProgressRequestDto courseProgressRequest = buildCourseProgress(enrollmentId, totalLessons);
            courseProgressService.createCourseProgress(courseProgressRequest);

            // Acknowledge message after successful course progress creation
            acknowledgeMessage(channel, deliveryTag, "Course progress created successfully");
            logSuccess(courseId, userId, startTime, deliveryTag, "Course progress created");
            
        } catch (IllegalArgumentException e) {
            // Validation errors - reject message without requeue
            logError(courseId, userId, startTime, deliveryTag, e, "Validation error");
            rejectMessage(channel, deliveryTag, false);
        } catch (Exception e) {
            // Business logic errors - reject message without requeue
            logError(courseId, userId, startTime, deliveryTag, e, "Processing error");
            rejectMessage(channel, deliveryTag, false);
        }
    }
    
    /**
     * Validates the payment completed event
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePaymentEvent(PaymentCompletedEventDto event) {
        if (event == null) {
            throw new IllegalArgumentException("PaymentCompletedEventDto cannot be null");
        }
        if (event.getCourseId() == null) {
            throw new IllegalArgumentException("CourseId cannot be null in PaymentCompletedEvent");
        }
        if (event.getUserId() == null) {
            throw new IllegalArgumentException("UserId cannot be null in PaymentCompletedEvent");
        }
    }
    
    /**
     * Builds enrollment request with proper statuses for paid enrollment
     */
    private EnrollmentRequestDto buildEnrollmentRequest(UUID enrollmentId, UUID courseId, UUID userId) {
        return EnrollmentRequestDto.builder()
            .enrollmentId(enrollmentId)
            .courseId(courseId)
            .studentId(userId)
            .enrolledAt(LocalDateTime.now())
            .enrollmentStatus(EnrollmentStatus.ACTIVE)
            .paymentStatus(PaymentStatus.PAID)
            .build();
    }
    
    private CourseProgressRequestDto buildCourseProgress(UUID enrollmentId, Integer totalLessons) {
        return CourseProgressRequestDto.builder()
            .enrollmentId(enrollmentId)
            .lessonsCompleted(0)
            .totalLessons(totalLessons)
            .build();
    }

    /**
     * Acknowledges the message
     */
    private void acknowledgeMessage(Channel channel, long deliveryTag, String reason) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.basicAck(deliveryTag, false);
                logger.debug("✅ [PAYMENT] Message acknowledged - DeliveryTag: {}, Reason: {}", deliveryTag, reason);
            } else {
                logger.warn("⚠️ [PAYMENT] Channel is closed, cannot acknowledge message - DeliveryTag: {}", deliveryTag);
            }
        } catch (IOException e) {
            logger.error("❌ [PAYMENT] Failed to acknowledge message - DeliveryTag: {}, Error: {}", 
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
                logger.warn("⚠️ [PAYMENT] Message rejected - DeliveryTag: {}, Requeue: {}", deliveryTag, requeue);
            } else {
                logger.warn("⚠️ [PAYMENT] Channel is closed, cannot reject message - DeliveryTag: {}", deliveryTag);
            }
        } catch (IOException e) {
            logger.error("❌ [PAYMENT] Failed to reject message - DeliveryTag: {}, Error: {}", 
                deliveryTag, e.getMessage(), e);
            throw new RuntimeException("Failed to reject message", e);
        }
    }
    
    /**
     * Logs successful processing
     */
    private void logSuccess(UUID courseId, UUID userId, long startTime, long deliveryTag, String action) {
        long processingTime = System.currentTimeMillis() - startTime;
        logger.info("✅ [PAYMENT] Successfully processed PaymentCompletedEvent - CourseId: {}, UserId: {}, Action: {}, ProcessingTime: {}ms, DeliveryTag: {}", 
            courseId, userId, action, processingTime, deliveryTag);
    }
    
    /**
     * Logs error during processing
     */
    private void logError(UUID courseId, UUID userId, long startTime, long deliveryTag, Exception e, String errorType) {
        long processingTime = System.currentTimeMillis() - startTime;
        logger.error("❌ [PAYMENT] Failed to process PaymentCompletedEvent - CourseId: {}, UserId: {}, ErrorType: {}, ProcessingTime: {}ms, DeliveryTag: {}, Error: {}", 
            courseId, userId, errorType, processingTime, deliveryTag, e.getMessage(), e);
    }
}
