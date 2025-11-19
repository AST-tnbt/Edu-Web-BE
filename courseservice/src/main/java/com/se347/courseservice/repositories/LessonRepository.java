package com.se347.courseservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
