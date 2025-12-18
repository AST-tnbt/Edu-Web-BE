package com.se347.courseservice.services;

import com.se347.courseservice.dtos.CourseResponseDto;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseQueryService {
    CourseResponseDto getCourseById(UUID courseId);
    CourseResponseDto getCourseByCourseSlug(String courseSlug);
    Page<CourseResponseDto> getAllCourses(Pageable pageable);
    List<CourseResponseDto> getCoursesByCategoryName(String categoryName);
    List<CourseResponseDto> getCoursesByInstructorId(UUID instructorId);
    List<CourseResponseDto> getCoursesByTitleContaining(String title);
    Integer getToltalLessonsByCourseId(UUID courseId);
}
