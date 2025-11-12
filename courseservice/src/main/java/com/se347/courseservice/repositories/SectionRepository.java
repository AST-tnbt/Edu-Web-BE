package com.se347.courseservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.se347.courseservice.entities.Section;
import java.util.UUID;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    List<Section> findByCourse_CourseId(UUID courseId);
    boolean existsByCourse_CourseIdAndTitle(UUID courseId, String title);
    Integer countByCourse_CourseId(UUID courseId);
}