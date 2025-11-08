package com.se347.courseservice.services;

import com.se347.courseservice.dtos.SectionRequestDto;
import com.se347.courseservice.dtos.SectionResponseDto;
import com.se347.courseservice.entities.Section;

import java.util.List;
import java.util.UUID;

public interface SectionService {
    SectionResponseDto createSection(SectionRequestDto request);
    SectionResponseDto getSectionById(UUID sectionId);
    Section toSection(UUID sectionId);
    SectionResponseDto updateSection(UUID sectionId, SectionRequestDto request);
    boolean sectionExists(UUID sectionId);
    List<SectionResponseDto> getSectionsByCourseId(UUID courseId);
}
