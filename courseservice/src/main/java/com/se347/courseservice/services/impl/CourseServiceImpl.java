package com.se347.courseservice.services.impl;

import com.se347.courseservice.dtos.CourseRequestDto;
import com.se347.courseservice.dtos.CourseResponseDto;
import com.se347.courseservice.dtos.CategoryRequestDto;
import com.se347.courseservice.services.CourseService;
import com.se347.courseservice.repositories.CourseRepository;
import com.se347.courseservice.services.CategoryService;
import com.se347.courseservice.entities.Course;
import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.repositories.SectionRepository;
import com.se347.courseservice.repositories.LessonRepository;
import com.se347.courseservice.entities.Section;

import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CategoryService categoryService;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    @Override
    public CourseResponseDto createCourse(CourseRequestDto request) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }

        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }

        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            throw new CourseException.InvalidRequestException("Description cannot be null or empty");
        }
        
        if (request.getThumbnailUrl() == null || request.getThumbnailUrl().isEmpty()) {
            throw new CourseException.InvalidRequestException("Thumbnail URL cannot be null or empty");
        }

        // Check if category exists (predefined or custom)
        if (!categoryService.categoryExists(request.getCategoryName())) {
            // Create new custom category
            CategoryRequestDto categoryRequest = CategoryRequestDto.builder()
                    .categoryName(request.getCategoryName())
                    .description("Auto-generated category")
                    .build();
            categoryService.createCategory(categoryRequest);
        }

        Course course = Course.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .thumbnailUrl(request.getThumbnailUrl())
            .price(request.getPrice())
            .level(request.getLevel())
            .categoryName(request.getCategoryName())
            .instructorId(request.getInstructorId())
            .build();

        course.onCreate();
        courseRepository.save(course);
        return mapToResponse(course);
    }

    @Override
    public CourseResponseDto getCourseById(UUID courseId) {
        if (courseId == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
               
        return mapToResponse(course);
    }

    @Override
    public CourseResponseDto updateCourse(UUID courseId, CourseRequestDto request) {
        if (courseId == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }
        
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }

        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }

        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            throw new CourseException.InvalidRequestException("Description cannot be null or empty");
        }
        
        if (request.getThumbnailUrl() == null || request.getThumbnailUrl().isEmpty()) {
            throw new CourseException.InvalidRequestException("Thumbnail URL cannot be null or empty");
        }

        // Check if course exists
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));

        // Check if category exists (predefined or custom)
        if (!categoryService.categoryExists(request.getCategoryName())) {
            throw new CourseException.CategoryNotFoundException(request.getCategoryName());
        }

        // Update existing course
        existingCourse.setTitle(request.getTitle());
        existingCourse.setDescription(request.getDescription());
        existingCourse.setThumbnailUrl(request.getThumbnailUrl());
        existingCourse.setPrice(request.getPrice());
        existingCourse.setLevel(request.getLevel());
        existingCourse.setCategoryName(request.getCategoryName());
        existingCourse.setInstructorId(request.getInstructorId());
        
        existingCourse.onUpdate();
        courseRepository.save(existingCourse);
        return mapToResponse(existingCourse);
    }

    @Override
    public boolean courseExists(UUID courseId) {
        return courseRepository.existsById(courseId);
    }

    @Override
    public List<CourseResponseDto> getAllCourses() {
        // Get all courses
        List<Course> courses = courseRepository.findAll();
        
        return courses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDto> getCoursesByCategoryName(String categoryName) {

        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Category name cannot be null or empty");
        }
        
        // Check if category exists (predefined or custom)
        if (!categoryService.categoryExists(categoryName)) {
            throw new CourseException.CategoryNotFoundException(categoryName);
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

        if (title == null || title.trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }
        
        // Get courses by title containing
        List<Course> courses = courseRepository.findByTitleContaining(title);
        
        return courses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Course toCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        return course;
    }

    @Override
    public Integer getToltalLessonsByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }

        if (!courseExists(courseId)) {
            throw new CourseException.CourseNotFoundException(courseId.toString());
        }

        int totalLessons = 0;
        List<Section> sections = sectionRepository.findByCourse_CourseId(courseId);
        for (Section section : sections) {
            totalLessons += (int) lessonRepository.countBySection_SectionId(section.getSectionId());
        }
        return totalLessons;
    }

    private CourseResponseDto mapToResponse(Course course) {
        return CourseResponseDto.builder()
            .courseId(course.getCourseId())
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
