package com.se347.courseservice.domains;

import java.util.List;
import java.util.UUID;
import com.se347.courseservice.entities.Course;

/**
 * Domain Service for Course Aggregate
 * 
 * PRINCIPLES (True DDD):
 * - ONLY contains complex business logic spanning multiple aggregates
 * - Does NOT contain CRUD operations (use Repository directly)
 * - Does NOT contain simple validations (use Entity guards)
 * - Does NOT create/update entities (use Entity factory methods)
 * 
 * WHAT MOVED TO ENTITIES:
 * - validateCourseCreation → Course.createNew() guards
 * - validateCourseUpdate → Course.updateDetails() guards
 * - createCourseEntity → Course.createNew()
 * - updateCourseEntity → Course.updateDetails()
 * - isCourseOwner → Course.isOwnedBy()
 * - validateCourseCanBeEdited → Course.ensureOwnedBy()
 * - calculateTotalLessons → Course.getTotalLessonsCount()
 * - validateCourseStructure → Course.hasMinimumContent()
 * - generateCourseSlug → Slug.fromTitle()
 * 
 * WHAT MOVED TO REPOSITORIES:
 * - findCourseById → courseRepository.findById()
 * - findCourseBySlug → courseRepository.findByCourseSlug()
 * - courseExists → courseRepository.existsById()
 * - courseExistsBySlug → courseRepository.findByCourseSlug().isPresent()
 * - findAllCourses → courseRepository.findAll()
 * 
 * This is how Domain Services should be: THIN!
 */
public interface CourseDomainService {
    
    // ========== CROSS-AGGREGATE OPERATIONS ==========
    
    /**
     * Ensure category exists (cross-aggregate with Category)
     * Auto-creates if needed (business rule)
     */
    void ensureCategoryExists(String categoryName);
    
    /**
     * Check if course title is unique across all courses
     */
    boolean isTitleUnique(String title);
    
    /**
     * Check if course title is unique (excluding specific course)
     * Used when updating course
     */
    boolean isTitleUniqueExcluding(String title, UUID courseId);
    
    // ========== COMPLEX BUSINESS WORKFLOWS ==========
    
    /**
     * Check if course can be published
     * Complex logic involving multiple aggregates and quality standards
     */
    boolean canPublishCourse(UUID courseId);
    
    /**
     * Check if course meets platform quality standards
     * 
     * Standards:
     * - Minimum 5 lessons
     * - Description >= 100 characters
     * - Has price set
     */
    boolean meetsQualityStandards(Course course);
    
    /**
     * Find courses requiring review
     * Complex query + business logic
     */
    List<Course> findCoursesRequiringReview();
}
