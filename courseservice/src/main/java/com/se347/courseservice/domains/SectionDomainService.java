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

    Section createSectionEntity(SectionRequestDto request, UUID courseId);
    Section updateSectionEntity(Section section, UUID courseId, SectionRequestDto request);

    void validateSectionCreation(SectionRequestDto request, UUID courseId);
    void validateSectionUpdate(Section section, UUID courseId, SectionRequestDto request, UUID userId);
    void validateSectionBelongsToCourse(Section section, UUID courseId);

    boolean isSectionOwner(Section section, UUID userId);

    String generateSectionSlug(String title);
}
