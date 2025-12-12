package com.se347.courseservice.services;

import com.se347.courseservice.dtos.CourseRequestDto;
import com.se347.courseservice.dtos.CourseResponseDto;
import com.se347.courseservice.entities.Course;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {
    CourseResponseDto createCourse(CourseRequestDto request, UUID userId);
    CourseResponseDto getCourseById(UUID courseId);
    CourseResponseDto getCourseByCourseSlug(String courseSlug);
    CourseResponseDto updateCourseById(UUID courseId, CourseRequestDto request, UUID userId);
    CourseResponseDto updateCourseByCourseSlug(String courseSlug, CourseRequestDto request, UUID userId);
    Page<CourseResponseDto> getAllCourses(Pageable pageable);
    List<CourseResponseDto> getCoursesByCategoryName(String categoryName);
    List<CourseResponseDto> getCoursesByInstructorId(UUID instructorId);
    List<CourseResponseDto> getCoursesByTitleContaining(String title);
    Integer getToltalLessonsByCourseId(UUID courseId);
    
    // Entity retrieval for internal use (delegates to domain service)
    Course toCourse(UUID courseId);
}
