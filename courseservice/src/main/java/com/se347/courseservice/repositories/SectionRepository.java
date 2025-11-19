package com.se347.courseservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.se347.courseservice.entities.Section;
import java.util.UUID;
import java.util.List;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    List<Section> findByCourse_CourseId(UUID courseId);
    List<Section> findByCourse_CourseSlug(String courseSlug);
    boolean existsByCourse_CourseIdAndTitle(UUID courseId, String title);
    Optional<Section> findBySectionSlug(String sectionSlug);
    Integer countByCourse_CourseId(UUID courseId);
}