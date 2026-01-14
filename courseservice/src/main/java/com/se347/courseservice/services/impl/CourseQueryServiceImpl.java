package com.se347.courseservice.services.impl;

import com.se347.courseservice.repositories.CourseRepository;
import com.se347.courseservice.services.CourseQueryService;
import com.se347.courseservice.dtos.CourseResponseDto;
import com.se347.courseservice.entities.Course;
import com.se347.courseservice.exceptions.CourseException;

import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CourseQueryServiceImpl implements CourseQueryService {
    
    private final CourseRepository courseRepository;

    /**
     * Get course by ID
     * 
     * DDD: Simple query - use Repository directly (no Domain Service needed)
     */
    @Override
    @Transactional(readOnly = true)
    public CourseResponseDto getCourseById(UUID courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        return mapToResponse(course);
    }

    /**
     * Get all courses with pagination
     * 
     * DDD: Simple query - use Repository directly
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponseDto> getAllCourses(Pageable pageable) {
        Page<Course> courses = courseRepository.findAll(pageable);
        return courses.map(this::mapToResponse);
    }

    @Override
    public List<CourseResponseDto> getCoursesByCategoryName(String categoryName) {

        if (categoryName == null || categoryName.isEmpty()) {
            throw new CourseException.InvalidRequestException("Category name cannot be null or empty");
        }

        // Get courses by category name
        List<Course> courses = courseRepository.findByCategoryName(categoryName);
        
        return courses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDto> getCoursesByInstructorId(UUID instructorId) {

        if (instructorId == null) {
            throw new CourseException.InvalidRequestException("Instructor ID cannot be null");
        }
        // Get courses by instructor id
        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        
        return courses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDto> getCoursesByTitleContaining(String title) {
        
        if (title == null || title.isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }

        // Get courses by title containing
        List<Course> courses = courseRepository.findByTitleContaining(title);
        
        return courses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get course by slug
     * 
     * DDD: Simple query - use Repository directly
     */
    @Override
    @Transactional(readOnly = true)
    public CourseResponseDto getCourseByCourseSlug(String courseSlug) {
        Course course = courseRepository.findByCourseSlug(courseSlug)
            .orElseThrow(() -> new CourseException.CourseNotFoundException("Course with slug '" + courseSlug + "' not found"));
        return mapToResponse(course);
    }

    /**
     * Get total lessons count for a course
     * 
     * DDD: Domain calculation - delegate to Course aggregate
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getToltalLessonsByCourseId(UUID courseId) {
        Course course = courseRepository.findByIdWithSections(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // Domain calculation method in aggregate root
        return course.getTotalLessonsCount();
    }

    private CourseResponseDto mapToResponse(Course course) {
        return CourseResponseDto.builder()
            .courseId(course.getCourseId())
            .courseSlug(course.getCourseSlug())
            .title(course.getTitle())
            .description(course.getDescription())
            .thumbnailUrl(course.getThumbnailUrl())
            .price(course.getPrice().getAmount()) // ← Value Object: need .getAmount()
            .level(course.getLevel())
            .categoryName(course.getCategoryName())
            .instructorId(course.getInstructorId())
            .createdAt(course.getCreatedAt())
            .updatedAt(course.getUpdatedAt())
            .build();
    }
}
