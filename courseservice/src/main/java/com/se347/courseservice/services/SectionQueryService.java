package com.se347.courseservice.services;

import com.se347.courseservice.dtos.SectionResponseDto;

import java.util.List;
import java.util.UUID;

public interface SectionQueryService {
    SectionResponseDto getSectionById(UUID sectionId);
    SectionResponseDto getSectionBySectionSlug(String sectionSlug);
    List<SectionResponseDto> getSectionsByCourseId(UUID courseId);
    List<SectionResponseDto> getSectionsByCourseSlug(String courseSlug);
}