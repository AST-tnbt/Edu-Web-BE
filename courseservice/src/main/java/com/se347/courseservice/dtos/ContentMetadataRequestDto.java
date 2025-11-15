package com.se347.courseservice.dtos;

import com.se347.courseservice.enums.ContentType;
import com.se347.courseservice.enums.ContentStatus;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentMetadataRequestDto {
    private UUID lessonId;
    private UUID contentId;
    private ContentType contentType;
    private String title;
    private String contentUrl;
    private String textContent;
    private int orderIndex;
    private ContentStatus status;
}
