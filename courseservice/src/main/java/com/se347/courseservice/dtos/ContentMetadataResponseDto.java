package com.se347.courseservice.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;
import java.time.LocalDateTime;

import com.se347.courseservice.enums.ContentType;
import com.se347.courseservice.enums.ContentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentMetadataResponseDto {
    private UUID contentId;
    private UUID lessonId;
    private ContentType contentType;
    private String title;
    private String contentUrl;
    private String textContent;
    private int orderIndex;
    private ContentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
