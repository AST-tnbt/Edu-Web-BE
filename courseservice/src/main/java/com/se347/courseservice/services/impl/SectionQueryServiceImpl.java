package com.se347.courseservice.services.impl;

import com.se347.courseservice.services.SectionQueryService;
import com.se347.courseservice.dtos.SectionResponseDto;
import com.se347.courseservice.repositories.SectionRepository;
import com.se347.courseservice.repositories.CourseRepository;
import com.se347.courseservice.entities.Section;
import com.se347.courseservice.entities.Course;
import com.se347.courseservice.exceptions.CourseException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service for Section operations
 * 
 * CRITICAL DDD PRINCIPLE: Section is part of Course aggregate!
 * 
 * RULES:
 * 1. ALL Section operations MUST go through Course aggregate root
 * 2. NEVER save Section directly (only save Course)
 * 3. Load Course aggregate, manipulate Section, save Course
 * 
 * WHY?
 * - Maintains aggregate consistency
 * - Course enforces invariants (e.g., duplicate section titles)
 * - Proper transaction boundary (entire aggregate)
 * 
 * WRONG (Bypassing aggregate):
 *   Section section = new Section(...);
 *   sectionRepository.save(section); ❌
 * 
 * RIGHT (Through aggregate):
 *   Course course = courseRepository.findById(...);
 *   Section section = course.addSection(...);
 *   courseRepository.save(course); ✅
 */
@RequiredArgsConstructor
@Service
public class SectionQueryServiceImpl implements SectionQueryService {
    
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository; // Only for queries

    /**
     * Get section by ID
     * 
     * DDD: Can query Section directly for read operations
     * (No need to load entire aggregate for read-only)
     */
    @Override
    @Transactional(readOnly = true)
    public SectionResponseDto getSectionById(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new CourseException.SectionNotFoundException(sectionId.toString()));
        
        return mapToResponse(section);
    }

    /**
     * Get section by slug
     */
    @Override
    @Transactional(readOnly = true)
    public SectionResponseDto getSectionBySectionSlug(String sectionSlug) {
        Section section = sectionRepository.findBySectionSlug(sectionSlug)
            .orElseThrow(() -> new CourseException.SectionNotFoundException("Section with slug '" + sectionSlug + "' not found"));
        
        return mapToResponse(section);
    }

    /**
     * Get all sections for a course by slug
     * 
     * DDD: Get sections from Course aggregate
     */
    @Override
    @Transactional(readOnly = true)
    public List<SectionResponseDto> getSectionsByCourseSlug(String courseSlug) {
        Course course = courseRepository.findByCourseSlug(courseSlug)
            .orElseThrow(() -> new CourseException.CourseNotFoundException("Course with slug '" + courseSlug + "' not found"));
        
        // Get sections from aggregate (read-only)
        return course.getSections().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get all sections for a course by ID
     */
    @Override
    @Transactional(readOnly = true)
    public List<SectionResponseDto> getSectionsByCourseId(UUID courseId) {
        Course course = courseRepository.findByIdWithSections(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // Get sections from aggregate
        return course.getSections().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Map Section entity to DTO
     * 
     * IMPORTANT: Value Objects need .getValue() to extract primitives
     */
    private SectionResponseDto mapToResponse(Section section) {
        return SectionResponseDto.builder()
            .sectionId(section.getSectionId())
            .sectionSlug(section.getSectionSlug().getValue()) // ← Value Object
            .courseId(section.getCourse().getCourseId())
            .title(section.getTitle())
            .description(section.getDescription())
            .orderIndex(section.getOrderIndex().getValue()) // ← Value Object
            .createdAt(section.getCreatedAt())
            .updatedAt(section.getUpdatedAt())
            .build();
    }
}
