package com.se347.courseservice.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonRequestDto {
    private String title;
    private UUID courseId;
    private int orderIndex;
}
