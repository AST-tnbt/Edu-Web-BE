package com.se347.courseservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.se347.courseservice.entities.Course;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    
    // ========== BASIC QUERIES ==========
    
    Optional<Course> findById(UUID courseId);
    List<Course> findByCategoryName(String categoryName);
    List<Course> findByInstructorId(UUID instructorId);
    List<Course> findByTitleContaining(String title);
    
    /**
     * Find course by exact title
     */
    Optional<Course> findByTitle(String title);
    
    /**
     * Find course by slug (using Value Object field)
     * Note: Access embedded value object with underscore notation
     */
    @Query("SELECT c FROM Course c WHERE c.courseSlug.value = :slugValue")
    Optional<Course> findByCourseSlug(@Param("slugValue") String slugValue);
    
    Page<Course> findAll(Pageable pageable);
    
    boolean existsByTitle(String title);
    boolean existsById(UUID courseId);
    
    // ========== DOMAIN-SPECIFIC QUERIES ==========
    
    /**
     * Find courses with eager-loaded sections
     * Used when need full aggregate
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.sections WHERE c.courseId = :courseId")
    Optional<Course> findByIdWithSections(@Param("courseId") UUID courseId);
    
    /**
     * Find recent courses with minimum content for review
     * Business logic: created in last 7 days, has at least one section
     */
    @Query("SELECT c FROM Course c WHERE SIZE(c.sections) > 0 " +
           "AND c.createdAt >= CURRENT_TIMESTAMP - 7 DAY " +
           "ORDER BY c.createdAt DESC")
    List<Course> findRecentCoursesWithMinimumContent();
    
    /**
     * Find courses by instructor with minimum enrollments
     * (For future use when enrollment tracking is added)
     */
    @Query("SELECT c FROM Course c WHERE c.instructorId = :instructorId " +
           "ORDER BY c.createdAt DESC")
    List<Course> findByInstructorIdOrderByCreatedAtDesc(@Param("instructorId") UUID instructorId);
}
