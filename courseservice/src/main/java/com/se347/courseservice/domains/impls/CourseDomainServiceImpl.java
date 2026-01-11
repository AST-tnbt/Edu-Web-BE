package com.se347.courseservice.domains.impls;

import com.se347.courseservice.services.CategoryService;
import com.se347.courseservice.repositories.CourseRepository;
import com.se347.courseservice.entities.Course;
import com.se347.courseservice.domains.CourseDomainService;
import com.se347.courseservice.exceptions.CourseException;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Domain Service for Course Aggregate
 * 
 * IMPORTANT: This service ONLY contains complex business logic that:
 * 1. Spans multiple aggregates (Course + Category)
 * 2. Requires external services
 * 3. Complex workflows that don't belong to single entity
 * 
 * MOVED TO ENTITIES:
 * - Simple validations → Course.createNew() guards
 * - Entity creation → Course.createNew() factory
 * - Entity updates → Course.updateDetails()
 * - Authorization → Course.ensureOwnedBy() / isOwnedBy()
 * - Calculations → Course.getTotalLessonsCount(), hasMinimumContent()
 * 
 * MOVED TO REPOSITORIES:
 * - CRUD operations → Use CourseRepository directly
 * - Queries → Use CourseRepository methods
 * 
 * This is how DDD Domain Services should be: THIN and focused on orchestration!
 */
@Service
@RequiredArgsConstructor
public class CourseDomainServiceImpl implements CourseDomainService {
    
    private final CourseRepository courseRepository;
    private final CategoryService categoryService;

    // ========== CROSS-AGGREGATE VALIDATIONS ==========
    
    /**
     * Validate category exists (cross-aggregate check with Category aggregate)
     * Auto-creates category if it doesn't exist (business rule)
     */
    @Override
    @Transactional
    public void ensureCategoryExists(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            throw new CourseException.InvalidRequestException("Category name cannot be null or empty");
        }
        
        if (!categoryService.categoryExists(categoryName)) {
            // Business rule: Auto-create category if doesn't exist
            com.se347.courseservice.dtos.CategoryRequestDto categoryRequest = 
                com.se347.courseservice.dtos.CategoryRequestDto.builder()
                    .categoryName(categoryName)
                    .description("Auto-generated category")
                    .build();
            categoryService.createCategory(categoryRequest);
        }
    }
    
    /**
     * Check if course title is unique (cross-entity check)
     * Used before creating new course
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isTitleUnique(String title) {
        if (title == null || title.isEmpty()) {
            return false;
        }
        return !courseRepository.existsByTitle(title);
    }
    
    /**
     * Check if course title is unique (excluding specific course)
     * Used when updating course title
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isTitleUniqueExcluding(String title, UUID courseId) {
        if (title == null || title.isEmpty()) {
            return false;
        }
        
        return courseRepository.findByTitle(title)
            .map(existingCourse -> existingCourse.getCourseId().equals(courseId))
            .orElse(true); // True if not found = unique
    }

    // ========== COMPLEX BUSINESS WORKFLOWS ==========
    
    /**
     * Check if course can be published
     * 
     * Complex business logic spanning:
     * - Course aggregate rules
     * - Category validation
     * - Quality standards
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canPublishCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // Delegate to entity for aggregate-specific rules
        if (!course.isReadyForPublish()) {
            return false;
        }
        
        // Cross-aggregate validation
        if (!categoryService.categoryExists(course.getCategoryName())) {
            return false;
        }
        
        // Platform-wide quality standards
        return meetsQualityStandards(course);
    }
    
    /**
     * Check if course meets platform quality standards
     * 
     * Business rules:
     * - Minimum 5 lessons
     * - Description at least 100 characters
     * - Has price set
     */
    @Override
    @Transactional(readOnly = true)
    public boolean meetsQualityStandards(Course course) {
        if (course == null) {
            return false;
        }
        
        // Minimum lesson count
        if (course.getTotalLessonsCount() < 5) {
            return false;
        }
        
        // Minimum description length
        if (course.getDescription() == null || course.getDescription().length() < 100) {
            return false;
        }
        
        // Must have price
        if (course.getPrice() == null || course.getPrice().isZero()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Find courses that need review (complex query + business logic)
     * 
     * Courses need review if:
     * - Recently created (< 7 days)
     * - Have minimum content
     * - Not yet published
     */
    @Override
    @Transactional(readOnly = true)
    public List<Course> findCoursesRequiringReview() {
        // Complex business query
        return courseRepository.findRecentCoursesWithMinimumContent()
            .stream()
            .filter(course -> course.hasMinimumContent())
            .filter(course -> !course.isReadyForPublish())
            .toList();
    }
}
