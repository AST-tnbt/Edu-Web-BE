package com.se347.enrollmentservice.dtos;

import com.se347.enrollmentservice.enums.EnrollmentStatus;
import com.se347.enrollmentservice.enums.PaymentStatus;

import java.time.LocalDateTime;
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponseDto {
    private UUID enrollmentId;
    private UUID courseId;
    private UUID studentId;
    private LocalDateTime enrolledAt;
    private EnrollmentStatus enrollmentStatus;
    private PaymentStatus paymentStatus;
    private LocalDateTime accessExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

