package com.se347.courseservice.dtos.events;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponseEventDto {
    private UUID enrollmentId;
    private UUID courseId;
}
