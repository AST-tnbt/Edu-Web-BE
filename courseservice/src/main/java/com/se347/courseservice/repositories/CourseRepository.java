package com.se347.courseservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.se347.courseservice.entities.Course;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByCategoryName(String categoryName);
    List<Course> findByInstructorId(UUID instructorId);
    List<Course> findByTitleContaining(String title);
    Optional<Course> findByCourseSlug(String courseSlug);
    boolean existsByTitle(String title);
}
