package com.se347.enrollmentservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.se347.enrollmentservice.entities.Enrollment;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    @Query("SELECT e FROM Enrollment e WHERE e.enrollmentId = :enrollmentId")
    Optional<Enrollment> findByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.courseId = :courseId")
    List<Enrollment> findByCourseId(@Param("courseId") UUID courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") UUID studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.courseId = :courseId AND e.studentId = :studentId")
    Enrollment findByCourseIdAndStudentId(@Param("courseId") UUID courseId, @Param("studentId") UUID studentId);
    
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.courseId = :courseId AND e.studentId = :studentId")
    boolean existsByCourseIdAndStudentId(@Param("courseId") UUID courseId, @Param("studentId") UUID studentId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.courseId = :courseId")
    boolean existsByCourseId(@Param("courseId") UUID courseId);
}
