package com.se347.courseservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.se347.courseservice.enums.ContentType;
import com.se347.courseservice.entities.Content;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByLessonId(Long lessonId);
    List<Content> findByTitleContaining(String title);
    List<Content> findByContentType(ContentType contentType);
}
