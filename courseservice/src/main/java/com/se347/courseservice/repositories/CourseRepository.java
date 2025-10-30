package com.se347.courseservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.se347.courseservice.entities.Course;
import java.util.UUID;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByCategoryName(String categoryName);
    List<Course> findByInstructorId(UUID instructorId);
    List<Course> findByTitleContaining(String title);
    boolean existsByTitle(String title);
}
