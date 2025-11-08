package com.se347.enrollmentservice.dtos.events;

import java.io.Serializable;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEventDto implements Serializable {
    private UUID userId;
    private UUID courseId;
}
