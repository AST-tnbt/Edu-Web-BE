package com.se347.courseservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.se347.courseservice.enums.ContentType;
import com.se347.courseservice.entities.Content;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<Content, UUID> {
    List<Content> findByContentId(UUID contentId);
    List<Content> findByLesson_LessonId(UUID lessonId);
    List<Content> findByTitleContaining(String title);
    List<Content> findByType(ContentType contentType);
}
