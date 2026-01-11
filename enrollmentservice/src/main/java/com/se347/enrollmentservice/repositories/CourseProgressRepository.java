package com.se347.enrollmentservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.se347.enrollmentservice.entities.CourseProgress;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, UUID> {

    @Query("SELECT cp FROM CourseProgress cp WHERE cp.courseProgressId = :courseProgressId")
    Optional<CourseProgress> findByCourseProgressId(@Param("courseProgressId") UUID courseProgressId);
    
    @Query("SELECT cp FROM CourseProgress cp WHERE cp.enrollment.enrollmentId = :enrollmentId")
    Optional<CourseProgress> findByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);
}
