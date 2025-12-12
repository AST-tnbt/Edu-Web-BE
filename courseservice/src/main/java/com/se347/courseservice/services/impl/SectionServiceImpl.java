package com.se347.courseservice.services.impl;

import com.se347.courseservice.services.SectionService;
import com.se347.courseservice.dtos.SectionRequestDto;
import com.se347.courseservice.dtos.SectionResponseDto;
import com.se347.courseservice.repositories.SectionRepository;
import com.se347.courseservice.entities.Section;
import com.se347.courseservice.domains.SectionDomainService;
import com.se347.courseservice.domains.CourseDomainService;
import com.se347.courseservice.exceptions.CourseException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SectionServiceImpl implements SectionService {
    private final SectionRepository sectionRepository;
    private final SectionDomainService sectionDomainService;
    private final CourseDomainService courseDomainService;

    @Override
    @Transactional
    public SectionResponseDto createSection(UUID courseId, SectionRequestDto request) {

        sectionDomainService.validateSectionCreation(request);
        courseDomainService.validateCourseExists(courseId);
        Section section = sectionDomainService.createSectionEntity(request);
        
        // Save through repository (infrastructure concern)
        sectionRepository.save(section);
        return mapToResponse(section);
    }

    @Override
    @Transactional(readOnly = true)
    public SectionResponseDto getSectionById(UUID courseId, UUID sectionId) {

        courseDomainService.validateCourseExists(courseId);
        sectionDomainService.validateSectionBelongsToCourse(sectionDomainService.findSectionById(sectionId), courseId);

        Section section = sectionDomainService.findSectionById(sectionId);
        
        return mapToResponse(section);
    }


    @Override
    @Transactional(readOnly = true)
    public SectionResponseDto getSectionBySectionSlug(String courseSlug, String sectionSlug) {

        courseDomainService.validateCourseExists(courseDomainService.findCourseBySlug(courseSlug).getCourseId());
        sectionDomainService.validateSectionBelongsToCourse(sectionDomainService.findSectionBySlug(sectionSlug), sectionDomainService.findSectionBySlug(sectionSlug).getCourse().getCourseId());

        Section section = sectionDomainService.findSectionBySlug(sectionSlug);

        return mapToResponse(section);
    }

    @Override
    @Transactional
    public SectionResponseDto updateSectionById(UUID courseId, UUID sectionId, SectionRequestDto request, UUID userId) {
        
        courseDomainService.validateCourseExists(courseId);
        sectionDomainService.validateSectionBelongsToCourse(sectionDomainService.findSectionById(sectionId), courseId);

        Section section = sectionDomainService.findSectionById(sectionId);

        if (!sectionDomainService.isSectionOwner(section, userId)) {
            throw new CourseException.UnauthorizedAccessException("User not authorized to access this resource");
        }

        sectionDomainService.validateSectionUpdate(section, request, userId);
        sectionDomainService.updateSectionEntity(section, request);
        sectionRepository.save(section);
        
        return mapToResponse(section);
    }

    @Override
    @Transactional
    public SectionResponseDto updateSectionBySectionSlug(String courseSlug, String sectionSlug, SectionRequestDto request, UUID userId) {

        courseDomainService.validateCourseExists(courseDomainService.findCourseBySlug(courseSlug).getCourseId());
        sectionDomainService.validateSectionBelongsToCourse(sectionDomainService.findSectionBySlug(sectionSlug), courseDomainService.findCourseBySlug(courseSlug).getCourseId());

        Section section = sectionDomainService.findSectionBySlug(sectionSlug);

        sectionDomainService.validateSectionUpdate(section, request, userId);
        sectionDomainService.updateSectionEntity(section, request);
        sectionRepository.save(section);

        return mapToResponse(section);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionResponseDto> getSectionsByCourseSlug(String courseSlug) {

        courseDomainService.validateCourseExists(courseDomainService.findCourseBySlug(courseSlug).getCourseId());
        List<Section> sections = sectionDomainService.findSectionsByCourseSlug(courseSlug);

        return sections.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionResponseDto> getSectionsByCourseId(UUID courseId) {

        courseDomainService.validateCourseExists(courseId);
        List<Section> sections = sectionDomainService.findSectionsByCourseId(courseId);

        return sections.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean sectionExists(UUID sectionId) {
        return sectionDomainService.sectionExists(sectionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Section toSection(UUID sectionId) {
        return sectionDomainService.toSection(sectionId);
    }

    private SectionResponseDto mapToResponse(Section section) {
        return SectionResponseDto.builder()
            .sectionId(section.getSectionId())
            .sectionSlug(section.getSectionSlug())
            .courseId(section.getCourse().getCourseId())
            .title(section.getTitle())
            .description(section.getDescription())
            .orderIndex(section.getOrderIndex())
            .createdAt(section.getCreatedAt())
            .updatedAt(section.getUpdatedAt())
            .build();
    }
}
