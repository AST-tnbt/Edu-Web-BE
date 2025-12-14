package com.se347.courseservice.domains.impls;

import com.se347.courseservice.services.CategoryService;
import com.se347.courseservice.repositories.CourseRepository;
import com.se347.courseservice.repositories.SectionRepository;
import com.se347.courseservice.repositories.LessonRepository;
import com.se347.courseservice.entities.Course;
import com.se347.courseservice.entities.Section;
import com.se347.courseservice.domains.CourseDomainService;
import com.se347.courseservice.dtos.CourseRequestDto;
import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.utils.SlugUtil;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;  
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class CourseDomainServiceImpl implements CourseDomainService {
    
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final CategoryService categoryService;

    // Course CRUD operations (trả về entity)
    @Override
    @Transactional(readOnly = true)
    public Course findCourseById(UUID courseId) {
        if (courseId == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public Course findCourseBySlug(String courseSlug) {
        if (courseSlug == null || courseSlug.isEmpty()) {
            throw new CourseException.InvalidRequestException("Course slug cannot be null or empty");
        }
        return courseRepository.findByCourseSlug(courseSlug)
                .orElseThrow(() -> new CourseException.CourseNotFoundException("Course with slug '" + courseSlug + "' not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Course toCourse(UUID courseId) {
        return findCourseById(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean courseExists(UUID courseId) {
        if (courseId == null) {
            return false;
        }
        return courseRepository.existsById(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean courseExistsBySlug(String courseSlug) {
        if (courseSlug == null || courseSlug.isEmpty()) {
            return false;
        }
        return courseRepository.findByCourseSlug(courseSlug).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean courseExistsByTitle(String title) {
        if (title == null || title.isEmpty()) {
            return false;
        }
        return courseRepository.existsByTitle(title);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> findAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    // Business validations
    @Override
    public void validateCourseCreation(CourseRequestDto request) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            throw new CourseException.InvalidRequestException("Description cannot be null or empty");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CourseException.InvalidRequestException("Price must be greater than 0");
        }
        if (request.getLevel() == null) {
            throw new CourseException.InvalidRequestException("Level cannot be null");
        }
        if (request.getCategoryName() == null || request.getCategoryName().isEmpty()) {
            throw new CourseException.InvalidRequestException("Category name cannot be null or empty");
        }
        if (courseExistsByTitle(request.getTitle())) {
            throw new CourseException.CourseAlreadyExistsException("Course with title '" + request.getTitle() + "' already exists");
        }
    }

    @Override
    public void validateCourseUpdate(Course course, CourseRequestDto request, UUID userId) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (course == null) {
            throw new CourseException.CourseNotFoundException("Course cannot be null");
        }
        
        // Validate basic fields
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            throw new CourseException.InvalidRequestException("Description cannot be null or empty");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CourseException.InvalidRequestException("Price must be greater than 0");
        }
        if (request.getLevel() == null) {
            throw new CourseException.InvalidRequestException("Level cannot be null");
        }
        if (request.getCategoryName() == null || request.getCategoryName().isEmpty()) {
            throw new CourseException.InvalidRequestException("Category name cannot be null or empty");
        }
        
        // Check title uniqueness only if title changed
        if (!course.getTitle().equals(request.getTitle()) && courseExistsByTitle(request.getTitle())) {
            throw new CourseException.CourseAlreadyExistsException("Course with title '" + request.getTitle() + "' already exists");
        }
        
        // Check authorization
        if (!isCourseOwner(course, userId)) {
            throw new CourseException.UnauthorizedAccessException("User not authorized to access this resource");
        }
    }

    @Override
    public Course updateCourseEntity(Course course, CourseRequestDto request) {
        if (course == null) {
            throw new CourseException.CourseNotFoundException("Course cannot be null");
        }
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
     
        // Update entity fields (business logic)
        course.setCourseSlug(generateCourseSlug(request.getTitle()));
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setPrice(request.getPrice());
        course.setLevel(request.getLevel());
        course.setCategoryName(request.getCategoryName());
        course.onUpdate();
        
        return course;
    }

    @Override
    public Course createCourseEntity(CourseRequestDto request, UUID userId) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (userId == null) {
            throw new CourseException.InvalidRequestException("Instructor ID cannot be null");
        }
        
        Course course = Course.builder()
            .courseSlug(generateCourseSlug(request.getTitle()))
            .title(request.getTitle())
            .description(request.getDescription())
            .thumbnailUrl(request.getThumbnailUrl())
            .price(request.getPrice())
            .level(request.getLevel())
            .categoryName(request.getCategoryName())
            .instructorId(userId)
            .build();
        
        course.onCreate();
        return course;
    }

    @Override
    public void validateCategoryExists(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            throw new CourseException.InvalidRequestException("Category name cannot be null or empty");
        }
        if (!categoryService.categoryExists(categoryName)) {
            // Auto-create category if it doesn't exist
            com.se347.courseservice.dtos.CategoryRequestDto categoryRequest = 
                com.se347.courseservice.dtos.CategoryRequestDto.builder()
                    .categoryName(categoryName)
                    .description("Auto-generated category")
                    .build();
            categoryService.createCategory(categoryRequest);
        }
    }

    @Override
    public void validateCourseCanBeEdited(Course course, UUID userId) {
        if (course == null) {
            throw new CourseException.CourseNotFoundException("Course cannot be null");
        }
        if (!isCourseOwner(course, userId)) {
            throw new CourseException.UnauthorizedAccessException("User not authorized to edit this course");
        }
    }

    @Override
    public void validateCourseExists(UUID courseId) {
        if (courseId == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }
        if (!courseExists(courseId)) {
            throw new CourseException.CourseNotFoundException(courseId.toString());
        }
    }

    @Override
    public boolean isCourseOwner(Course course, UUID userId) {
        if (course == null || userId == null) {
            return false;
        }
        return course.getInstructorId().equals(userId);
    }

    // Aggregations & Calculations
    @Override
    @Transactional(readOnly = true)
    public Integer calculateTotalLessons(UUID courseId) {
        if (courseId == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }
        
        if (!courseExists(courseId)) {
            throw new CourseException.CourseNotFoundException(courseId.toString());
        }

        List<Section> sections = sectionRepository.findByCourse_CourseId(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));

        int totalLessons = 0;
        for (Section section : sections) {
            totalLessons += (int) lessonRepository.countBySection_SectionId(section.getSectionId());
        }
        return totalLessons;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateCourseStructure(Course course) {
        if (course == null) {
            return false;
        }
        // Validate course has at least one section
        List<Section> sections = sectionRepository.findByCourse_CourseId(course.getCourseId())
            .orElse(List.of());
        return !sections.isEmpty();
    }

    // Slug management
    @Override
    public String generateCourseSlug(String title) {
        if (title == null || title.isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }
        return SlugUtil.toSlug(title);
    }
}
