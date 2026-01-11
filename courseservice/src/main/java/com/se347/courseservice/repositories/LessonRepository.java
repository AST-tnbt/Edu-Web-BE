package com.se347.courseservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.se347.courseservice.entities.Lesson;
import java.util.UUID;
import java.util.List;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    boolean existsBySection_SectionIdAndTitle(UUID sectionId, String title);
    List<Lesson> findBySection_SectionId(UUID sectionId);
    Optional<Lesson> findByLessonSlug(String lessonSlug);
    long countBySection_SectionId(UUID sectionId);
    
    /**
     * Find Lesson with its Contents (eager loading)
     * 
     * Use case: When creating/updating Content, we need to validate
     * against existing contents in the lesson (e.g., no duplicate titles)
     * 
     * Performance: Avoids N+1 query problem
     */
    @Query("SELECT l FROM Lesson l " +
           "LEFT JOIN FETCH l.contents " +
           "WHERE l.lessonId = :lessonId")
    Optional<Lesson> findByIdWithContents(@Param("lessonId") UUID lessonId);
}
