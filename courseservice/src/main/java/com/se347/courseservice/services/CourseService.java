package com.se347.courseservice.services;

import com.se347.courseservice.dtos.CourseRequestDto;
import com.se347.courseservice.dtos.CourseResponseDto;
import com.se347.courseservice.entities.Course;
import java.util.UUID;
import java.util.List;

public interface CourseService {
    CourseResponseDto createCourse(CourseRequestDto request);
    CourseResponseDto getCourseById(UUID courseId);
    CourseResponseDto updateCourse(UUID courseId, CourseRequestDto request);
    Course toCourse(UUID courseId);
    boolean courseExists(UUID courseId);
    List<CourseResponseDto> getAllCourses();
    List<CourseResponseDto> getCoursesByCategoryName(String categoryName);
    List<CourseResponseDto> getCoursesByInstructorId(UUID instructorId);
    List<CourseResponseDto> getCoursesByTitleContaining(String title);
    CourseResponseDto getCourseByCourseSlug(String courseSlug);
    Integer getToltalLessonsByCourseId(UUID courseId);
    CourseResponseDto updateCourseByCourseSlug(String courseSlug, CourseRequestDto request, String userRoles, UUID userId);
}
