package com.se347.courseservice.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResponseDto {
    private UUID lessonId;
    private String title;
    private UUID sectionId;
    private int orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
