package com.se347.enrollmentservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.se347.enrollmentservice.entities.Enrollment;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    List<Enrollment> findByEnrollmentId(UUID enrollmentId);
    List<Enrollment> findByCourseId(UUID courseId);
    List<Enrollment> findByStudentId(UUID studentId);
    List<Enrollment> findByCourseIdAndStudentId(UUID courseId, UUID studentId);
}
