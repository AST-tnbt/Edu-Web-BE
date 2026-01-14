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
public class ContentMetadataRequestDto {
    private UUID lessonId;
    private UUID contentId;
    private String contentUrl;
    private int orderIndex;
}
