package com.se347.courseservice.services.impl;

import com.se347.courseservice.dtos.LessonResponseDto;
import com.se347.courseservice.entities.Lesson;
import com.se347.courseservice.entities.Section;
import com.se347.courseservice.services.LessonQueryService;
import com.se347.courseservice.repositories.LessonRepository;
import com.se347.courseservice.repositories.SectionRepository;
import com.se347.courseservice.exceptions.CourseException;

import java.util.List;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service for Lesson operations
 * 
 * CRITICAL DDD PRINCIPLE: Lesson is part of Course aggregate!
 * 
 * Aggregate Hierarchy:
 * Course (root) → Section → Lesson → Content
 * 
 * RULES:
 * 1. ALL Lesson operations MUST go through Course aggregate
 * 2. Load Course → Find Section → Manipulate Lesson → Save Course
 * 3. NEVER save Lesson directly (cascade from Course)
 * 
 * WHY?
 * - Maintains aggregate consistency
 * - Section enforces invariants (duplicate lesson titles)
 * - Proper transaction boundary
 */
@RequiredArgsConstructor
@Service
public class LessonQueryServiceImpl implements LessonQueryService {
    
    private final SectionRepository sectionRepository; // For queries only
    private final LessonRepository lessonRepository; // For queries only

    /**
     * Get lesson by ID
     * 
     * DDD: Can query Lesson directly for read operations
     */
    @Override
    @Transactional(readOnly = true)
    public LessonResponseDto getLessonById(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new CourseException.LessonNotFoundException(lessonId.toString()));
        
        return mapToResponse(lesson);
    }

    /**
     * Get all lessons for a section
     */
    @Override
    @Transactional(readOnly = true)
    public List<LessonResponseDto> getLessonsBySectionId(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new CourseException.SectionNotFoundException(sectionId.toString()));
        
        // Get lessons from Section entity
        return section.getLessons().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get lesson by slug
     */
    @Override
    @Transactional(readOnly = true)
    public LessonResponseDto getLessonByLessonSlug(String lessonSlug) {
        Lesson lesson = lessonRepository.findByLessonSlug(lessonSlug)
            .orElseThrow(() -> new CourseException.LessonNotFoundException("Lesson with slug '" + lessonSlug + "' not found"));
        
        return mapToResponse(lesson);
    }

    /**
     * Get lessons by section slug
     */
    @Override
    @Transactional(readOnly = true)
    public List<LessonResponseDto> getLessonsBySectionSlug(String sectionSlug) {
        Section section = sectionRepository.findBySectionSlug(sectionSlug)
            .orElseThrow(() -> new CourseException.SectionNotFoundException("Section with slug '" + sectionSlug + "' not found"));
        
        return section.getLessons().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }  

    /**
     * Map Lesson entity to DTO
     * 
     * IMPORTANT: Value Objects need .getValue() to extract primitives
     */
    private LessonResponseDto mapToResponse(Lesson lesson) {
        return LessonResponseDto.builder()
            .lessonId(lesson.getLessonId())
            .lessonSlug(lesson.getLessonSlug())
            .title(lesson.getTitle())
            .sectionId(lesson.getSection().getSectionId())
            .orderIndex(lesson.getOrderIndex().getValue()) // ← Value Object
            .createdAt(lesson.getCreatedAt())
            .updatedAt(lesson.getUpdatedAt())
            .build();
    }
}
