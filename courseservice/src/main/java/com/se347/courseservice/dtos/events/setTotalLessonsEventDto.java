package com.se347.courseservice.dtos.events;

import java.io.Serializable;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class setTotalLessonsEventDto implements Serializable {
    private UUID courseId;
    private Integer totalLessons;
}
