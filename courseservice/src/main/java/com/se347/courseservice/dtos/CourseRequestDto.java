package com.se347.courseservice.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.UUID;
import com.se347.courseservice.enums.CourseLevel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequestDto {
    private UUID courseId;
    private String courseSlug;
    private String title;
    private String description;
    private String thumbnailUrl;
    private BigDecimal price;
    private CourseLevel level;
    private String categoryName;
    private UUID instructorId;
}
