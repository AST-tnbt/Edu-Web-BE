package com.se347.courseservice.domains;

import java.util.UUID;
import com.se347.courseservice.entities.Section;
import com.se347.courseservice.dtos.SectionRequestDto;
import java.util.List;

public interface SectionDomainService {
    Section findSectionById(UUID sectionId);
    Section findSectionBySlug(String sectionSlug);
    List<Section> findSectionsByCourseSlug(String courseSlug);
    List<Section> findSectionsByCourseId(UUID courseId);

    Section toSection(UUID sectionId);
    boolean sectionExists(UUID sectionId);
    boolean sectionExistsBySlug(String sectionSlug);
    boolean sectionExistsByTitle(String title);

    Section createSectionEntity(SectionRequestDto request);
    Section updateSectionEntity(Section section, SectionRequestDto request);

    void validateSectionCreation(SectionRequestDto request);
    void validateSectionUpdate(Section section, SectionRequestDto request, UUID userId);
    void validateSectionBelongsToCourse(Section section, UUID courseId);

    boolean isSectionOwner(Section section, UUID userId);

    String generateSectionSlug(String title);
}
