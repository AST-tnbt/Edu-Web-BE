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
public class SectionRequestDto {    
    private UUID sectionId;
    private UUID courseId;
    private String title;
    private String description;
    private int orderIndex;
}
