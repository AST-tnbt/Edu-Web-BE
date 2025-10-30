package com.se347.courseservice.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;

import com.se347.courseservice.enums.ContentType;
import com.se347.courseservice.enums.ContentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentMetadataResponseDto {
    private UUID contentMetadataId;
    private UUID courseId;
    private UUID lessonId;
    private UUID contentId;
    private ContentType contentType;
    private String title;
    private String contentUrl;
    private String textContent;
    private int orderIndex;
    private ContentStatus status;
    private String createdAt;
    private String updatedAt;
}
