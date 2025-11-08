package com.se347.enrollmentservice.dtos.events;

import java.io.Serializable;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalLessonsEventDto implements Serializable {
    private UUID courseId;
    private Integer totalLessons;
}
