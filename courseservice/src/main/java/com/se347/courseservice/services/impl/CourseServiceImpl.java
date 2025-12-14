package com.se347.courseservice.services.impl;

import com.se347.courseservice.domains.CourseDomainService;
import com.se347.courseservice.dtos.CourseRequestDto;
import com.se347.courseservice.dtos.CourseResponseDto;
import com.se347.courseservice.services.CourseService;
import com.se347.courseservice.repositories.CourseRepository;
import com.se347.courseservice.entities.Course;
import com.se347.courseservice.exceptions.CourseException;

import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseDomainService courseDomainService;

    @Transactional
    @Override
    public CourseResponseDto createCourse(CourseRequestDto request, UUID userId) {
        // Validate business rules through domain service
        courseDomainService.validateCourseCreation(request);
        courseDomainService.validateCategoryExists(request.getCategoryName());
        
        // Create entity through domain service
        Course course = courseDomainService.createCourseEntity(request, userId);
        
        // Save through repository (infrastructure concern)
        courseRepository.save(course);
        return mapToResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDto getCourseById(UUID courseId) {
        Course course = courseDomainService.findCourseById(courseId);
        return mapToResponse(course);
    }

    @Transactional
    @Override
    public CourseResponseDto updateCourseById(UUID courseId, CourseRequestDto request, UUID userId) {
        // Get course through domain service
        Course existingCourse = courseDomainService.findCourseById(courseId);
        
        // Validate business rules through domain service
        courseDomainService.validateCourseUpdate(existingCourse, request, userId);
        courseDomainService.validateCategoryExists(request.getCategoryName());

        // Update entity through domain service
        courseDomainService.updateCourseEntity(existingCourse, request);
        
        // Save through repository (infrastructure concern)
        courseRepository.save(existingCourse);
        return mapToResponse(existingCourse);
    }

    @Transactional
    @Override
    public CourseResponseDto updateCourseByCourseSlug(String courseSlug, CourseRequestDto request, UUID userId) {
        // Get course through domain service
        Course course = courseDomainService.findCourseBySlug(courseSlug);
        
        // Validate business rules through domain service
        courseDomainService.validateCourseUpdate(course, request, userId);
        courseDomainService.validateCategoryExists(request.getCategoryName());

        // Update entity through domain service
        courseDomainService.updateCourseEntity(course, request);
        
        // Save through repository (infrastructure concern)
        courseRepository.save(course);
        return mapToResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponseDto> getAllCourses(Pageable pageable) {
        
        Page<Course> courses = courseDomainService.findAllCourses(pageable);

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

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDto getCourseByCourseSlug(String courseSlug) {
        Course course = courseDomainService.findCourseBySlug(courseSlug);
        return mapToResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getToltalLessonsByCourseId(UUID courseId) {
        return courseDomainService.calculateTotalLessons(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Course toCourse(UUID courseId) {
        return courseDomainService.toCourse(courseId);
    }

    private CourseResponseDto mapToResponse(Course course) {
        return CourseResponseDto.builder()
            .courseId(course.getCourseId())
            .courseSlug(course.getCourseSlug())
            .title(course.getTitle())
            .description(course.getDescription())
            .thumbnailUrl(course.getThumbnailUrl())
            .price(course.getPrice())
            .level(course.getLevel())
            .categoryName(course.getCategoryName())
            .instructorId(course.getInstructorId())
            .createdAt(course.getCreatedAt())
            .updatedAt(course.getUpdatedAt())
            .build();
    }
}
