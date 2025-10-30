package com.se347.enrollmentservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.se347.enrollmentservice.entities.CourseProgress;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, UUID> {
    CourseProgress findByEnrollmentId(UUID enrollmentId);
    CourseProgress findByCourseProgressId(UUID courseProgressId);
}
