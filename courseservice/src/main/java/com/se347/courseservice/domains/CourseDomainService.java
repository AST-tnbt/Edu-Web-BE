package com.se347.courseservice.domains;

import java.util.UUID;
import com.se347.courseservice.entities.Course;
import com.se347.courseservice.dtos.CourseRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseDomainService {
    // Course CRUD operations (trả về entity)
    Course findCourseById(UUID courseId);
    Course findCourseBySlug(String courseSlug);
    Course toCourse(UUID courseId); // Alias for findCourseById for compatibility
    boolean courseExists(UUID courseId);
    boolean courseExistsBySlug(String courseSlug);
    boolean courseExistsByTitle(String title);
    
    // Business validations
    void validateCourseCreation(CourseRequestDto request);
    void validateCourseUpdate(Course course, CourseRequestDto request, UUID userId);
    void validateCourseExists(UUID courseId);
    void validateCategoryExists(String categoryName);
    void validateCourseCanBeEdited(Course course, UUID userId);
    
    // Entity update operations
    Course updateCourseEntity(Course course, CourseRequestDto request);
    Course createCourseEntity(CourseRequestDto request, UUID userId);
    
    // Authorization
    boolean isCourseOwner(Course course, UUID userId);
    
    Page<Course> findAllCourses(Pageable pageable);

    // Aggregations & Calculations
    Integer calculateTotalLessons(UUID courseId);
    boolean validateCourseStructure(Course course);
    
    // Slug management
    String generateCourseSlug(String title);
}
