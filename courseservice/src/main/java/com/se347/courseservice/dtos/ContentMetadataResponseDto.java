package com.se347.courseservice.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentMetadataResponseDto {
    private UUID contentId;
    private UUID lessonId;
    private String contentUrl;
    private int orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
