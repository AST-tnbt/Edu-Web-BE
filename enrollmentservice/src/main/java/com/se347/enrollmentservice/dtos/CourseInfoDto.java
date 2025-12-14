package com.se347.enrollmentservice.dtos;

import lombok.Data;
import java.util.UUID;

@Data
public class CourseInfoDto {
    private UUID courseId;
    private UUID instructorId;
}

