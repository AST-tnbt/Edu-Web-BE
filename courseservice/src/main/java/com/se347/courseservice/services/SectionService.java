package com.se347.courseservice.services;

import com.se347.courseservice.dtos.SectionRequestDto;
import com.se347.courseservice.dtos.SectionResponseDto;
import com.se347.courseservice.entities.Section;

import java.util.List;
import java.util.UUID;

public interface SectionService {
    SectionResponseDto createSection(UUID courseId, SectionRequestDto request);
    SectionResponseDto getSectionById(UUID courseId, UUID sectionId);
    SectionResponseDto getSectionBySectionSlug(String courseSlug, String sectionSlug);
    SectionResponseDto updateSectionById(UUID courseId, UUID sectionId, SectionRequestDto request, UUID userId);
    SectionResponseDto updateSectionBySectionSlug(String courseSlug, String sectionSlug, SectionRequestDto request, UUID userId);
    List<SectionResponseDto> getSectionsByCourseId(UUID courseId);
    List<SectionResponseDto> getSectionsByCourseSlug(String courseSlug);
    boolean sectionExists(UUID sectionId);

    Section toSection(UUID sectionId);
}
