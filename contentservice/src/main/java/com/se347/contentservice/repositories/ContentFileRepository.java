package com.se347.contentservice.repositories;

import com.se347.contentservice.entities.ContentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContentFileRepository extends JpaRepository<ContentFile, UUID> {
    List<ContentFile> findByContentId(UUID contentId);
}
