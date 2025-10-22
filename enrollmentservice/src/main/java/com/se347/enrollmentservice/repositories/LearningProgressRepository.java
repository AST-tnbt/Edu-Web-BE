package com.se347.enrollmentservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.se347.enrollmentservice.entities.LearningProgress;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, UUID> {
    List<LearningProgress> findByEnrollmentId(UUID enrollmentId);
    LearningProgress findByLearningProgressId(UUID learningProgressId);
    LearningProgress findByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId);
}
