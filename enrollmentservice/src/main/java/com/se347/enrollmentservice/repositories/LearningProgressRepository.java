package com.se347.enrollmentservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.se347.enrollmentservice.entities.LearningProgress;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, UUID> {
    @Query("SELECT lp FROM LearningProgress lp WHERE lp.enrollment.enrollmentId = :enrollmentId")
    Optional<List<LearningProgress>> findByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);
    
    @Query("SELECT lp FROM LearningProgress lp WHERE lp.learningProgressId = :learningProgressId")
    Optional<LearningProgress> findByLearningProgressId(@Param("learningProgressId") UUID learningProgressId);
    
    @Query("SELECT lp FROM LearningProgress lp WHERE lp.lessonId = :lessonId AND lp.enrollment.enrollmentId = :enrollmentId")
    Optional<LearningProgress> findByLessonIdAndEnrollmentId(@Param("lessonId") UUID lessonId, @Param("enrollmentId") UUID enrollmentId);
}
