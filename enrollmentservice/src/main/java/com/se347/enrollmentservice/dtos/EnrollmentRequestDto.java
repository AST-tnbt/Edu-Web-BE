package com.se347.enrollmentservice.dtos;

import com.se347.enrollmentservice.enums.EnrollmentStatus;

import java.time.LocalDateTime;
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRequestDto {
    private UUID enrollmentId;
    private UUID courseId;
    private String courseSlug;
    private UUID studentId;
    private LocalDateTime enrolledAt;
    private EnrollmentStatus enrollmentStatus;
}
