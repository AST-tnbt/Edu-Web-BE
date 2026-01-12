package com.se347.enrollmentservice.listeners.impl;

import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.events.PaymentCompletedEventDto;
import com.se347.enrollmentservice.listeners.PaymentListener;
import com.se347.enrollmentservice.services.EnrollmentCommandService;
import com.se347.enrollmentservice.services.EnrollmentQueryService;
import com.se347.enrollmentservice.enums.EnrollmentStatus;

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
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentListenerImpl implements PaymentListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentListenerImpl.class);
    private final EnrollmentQueryService enrollmentQueryService;
    private final EnrollmentCommandService enrollmentCommandService;

    @Transactional
    @RabbitListener(queues = "${app.rabbitmq.queue.payment-completed}", containerFactory = "rabbitListenerContainerFactory")
    public void handlePaymentCompletedEvent(PaymentCompletedEventDto paymentCompletedEventDto,
                                            Channel channel,
                                            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        logger.info("🔔 [PAYMENT] Received PaymentCompletedEvent - DeliveryTag: {}, Event: {}", 
            deliveryTag, paymentCompletedEventDto != null ? paymentCompletedEventDto.toString() : "NULL");
        
        long startTime = System.currentTimeMillis();
        
        if (paymentCompletedEventDto == null) {
            logger.error("❌ [PAYMENT] PaymentCompletedEventDto is NULL - DeliveryTag: {}", deliveryTag);
            rejectMessage(channel, deliveryTag, false);
            return;
        }
        
        UUID courseId = paymentCompletedEventDto.getCourseId();
        UUID userId = paymentCompletedEventDto.getUserId();
        UUID instructorId = paymentCompletedEventDto.getInstructorId();
        String courseSlug = paymentCompletedEventDto.getCourseSlug();
        
        try {
            // Validate input
            validatePaymentEvent(paymentCompletedEventDto);
            
            // Check if enrollment already exists (idempotency check)
            boolean enrollmentExists = enrollmentQueryService.isEnrollmentExists(courseId, userId);
            
            if (enrollmentExists) {
                logger.info("✅ [PAYMENT] Enrollment already exists (idempotent) - CourseId: {}, UserId: {}", 
                    courseId, userId);
                // Acknowledge message even if enrollment exists (idempotent operation)
                acknowledgeMessage(channel, deliveryTag, "Enrollment already exists");
                logSuccess(courseId, userId, startTime, deliveryTag, "Idempotent - enrollment exists");
                return;
            }

            // Create new enrollment with proper statuses
            EnrollmentRequestDto enrollmentRequest = buildEnrollmentRequest(courseId, userId, instructorId, courseSlug);
            enrollmentCommandService.createEnrollment(enrollmentRequest);
            
            // Acknowledge message after successful enrollment creation
            acknowledgeMessage(channel, deliveryTag, "Enrollment created successfully");
            logSuccess(courseId, userId, startTime, deliveryTag, "Enrollment created");
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
    private EnrollmentRequestDto buildEnrollmentRequest(UUID courseId, UUID userId, UUID instructorId, String courseSlug) {
        return EnrollmentRequestDto.builder()
            .courseId(courseId)
            .studentId(userId)
            .instructorId(instructorId)
            .courseSlug(courseSlug)
            .enrolledAt(LocalDateTime.now())
            .enrollmentStatus(EnrollmentStatus.ACTIVE)
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
