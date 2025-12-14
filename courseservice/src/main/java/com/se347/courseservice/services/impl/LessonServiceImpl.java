package com.se347.courseservice.services.impl;

import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.dtos.LessonResponseDto;
import com.se347.courseservice.entities.Lesson;
import com.se347.courseservice.domains.LessonDomainService;
import com.se347.courseservice.domains.SectionDomainService;
import com.se347.courseservice.domains.CourseDomainService;
import com.se347.courseservice.services.LessonService;
import com.se347.courseservice.repositories.LessonRepository;
import com.se347.courseservice.clients.EnrollmentServiceClient;
import com.se347.courseservice.publishers.CoursePublisher;
import com.se347.courseservice.dtos.events.setTotalLessonsEventDto;
import com.se347.courseservice.services.CourseService;
import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.utils.SlugUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final LessonDomainService lessonDomainService;
    private final SectionDomainService sectionDomainService;
    private final CourseDomainService courseDomainService;
    private final EnrollmentServiceClient enrollmentServiceClient;
    private final CoursePublisher coursePublisher;
    private final CourseService courseService;
    private final Logger logger = LoggerFactory.getLogger(LessonServiceImpl.class);
    
    @Override
    @Transactional
    public LessonResponseDto createLesson(UUID courseId, UUID sectionId, LessonRequestDto request) {
        // Validate business rules through domain service
        lessonDomainService.validateLessonCreation(request);
        courseDomainService.validateCourseExists(courseId);
        sectionDomainService.validateSectionBelongsToCourse(sectionDomainService.findSectionById(sectionId), courseId);

        // Create entity through domain service
        Lesson lesson = lessonDomainService.createLessonEntity(request, sectionDomainService.findSectionById(sectionId));
        
        // Save through repository (infrastructure concern)
        lessonRepository.save(lesson);

        // Check enrollments với error handling
        try {
            if (!enrollmentServiceClient.getEnrollmentsbyCourseId(courseId).isEmpty()) {
                Integer totalLessons = courseService.getToltalLessonsByCourseId(courseId);
                setTotalLessonsEventDto event = setTotalLessonsEventDto.builder()
                    .courseId(courseId)
                    .totalLessons(totalLessons)
                    .build();
                coursePublisher.publishSetTotalLessonsEvent(event);
            }
        } catch (Exception e) {
            logger.warn("Failed to check enrollments or publish event for course: {}", courseId, e);
            // Không throw exception để không rollback transaction
        }

        return mapToResponse(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResponseDto getLessonById(UUID courseId, UUID sectionId, UUID lessonId) {
        courseDomainService.validateCourseExists(courseId);
        sectionDomainService.validateSectionBelongsToCourse(sectionDomainService.findSectionById(sectionId), courseId);
        lessonDomainService.validateLessonBelongsToSection(lessonDomainService.findLessonById(lessonId), sectionDomainService.findSectionById(sectionId));

        Lesson lesson = lessonDomainService.findLessonById(lessonId);
        return mapToResponse(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonResponseDto> getLessonsBySectionId(UUID courseId, UUID sectionId) {
        courseDomainService.validateCourseExists(courseId);
        sectionDomainService.validateSectionBelongsToCourse(sectionDomainService.findSectionById(sectionId), courseId);

        List<Lesson> lessons = lessonDomainService.findLessonsBySectionId(sectionId);
        
        if (lessons.isEmpty()) {
            throw new CourseException.LessonNotFoundException("Lessons with section ID '" + sectionId + "' not found");
        }

        return lessons.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResponseDto getLessonByLessonSlug(String courseSlug, String sectionSlug, String lessonSlug) {
        UUID courseId = courseDomainService.findCourseBySlug(courseSlug).getCourseId();
        courseDomainService.validateCourseExists(courseId);
        sectionDomainService.validateSectionBelongsToCourse(sectionDomainService.findSectionBySlug(sectionSlug), courseId);
        lessonDomainService.validateLessonBelongsToSection(lessonDomainService.findLessonByLessonSlug(lessonSlug), sectionDomainService.findSectionBySlug(sectionSlug));

        Lesson lesson = lessonDomainService.findLessonByLessonSlug(lessonSlug);
        return mapToResponse(lesson);
    }

    @Transactional
    @Override
    public LessonResponseDto updateLessonByLessonSlug(String courseSlug, String sectionSlug, String lessonSlug, LessonRequestDto request, UUID userId) {
        UUID courseId = courseDomainService.findCourseBySlug(courseSlug).getCourseId();
        courseDomainService.validateCourseExists(courseId);
        sectionDomainService.validateSectionBelongsToCourse(sectionDomainService.findSectionBySlug(sectionSlug), courseId);
        lessonDomainService.validateLessonBelongsToSection(lessonDomainService.findLessonByLessonSlug(lessonSlug), sectionDomainService.findSectionBySlug(sectionSlug));

        // Get lesson through domain service
        Lesson lesson = lessonDomainService.findLessonByLessonSlug(lessonSlug);
        
        lessonDomainService.validateLessonUpdate(lesson, request, userId);
        
        // Update entity through domain service
        lessonDomainService.updateLessonEntity(lesson, request, lesson.getSection());
        
        // Save through repository (infrastructure concern)
        lessonRepository.save(lesson);

        return mapToResponse(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public Lesson toLesson(UUID lessonId) {
        return lessonDomainService.toLesson(lessonId);
    }

    @Override
    @Transactional
    public LessonResponseDto updateLessonById(UUID courseId, UUID sectionId, UUID lessonId, LessonRequestDto request, UUID userId) {
        courseDomainService.validateCourseExists(courseId);
        sectionDomainService.validateSectionBelongsToCourse(sectionDomainService.findSectionById(sectionId), courseId);
        lessonDomainService.validateLessonBelongsToSection(lessonDomainService.findLessonById(lessonId), sectionDomainService.findSectionById(sectionId));

        // Get lesson through domain service
        Lesson existingLesson = lessonDomainService.findLessonById(lessonId);
        
        // Validate business rules through domain service
        lessonDomainService.validateLessonUpdate(existingLesson, request, userId);
                
        // Update entity through domain service
        lessonDomainService.updateLessonEntity(existingLesson, request, existingLesson.getSection());
        
        // Save through repository (infrastructure concern)
        lessonRepository.save(existingLesson);

        return mapToResponse(existingLesson);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonResponseDto> getLessonsBySectionSlug(String courseSlug, String sectionSlug) {
        UUID courseId = courseDomainService.findCourseBySlug(courseSlug).getCourseId();
        courseDomainService.validateCourseExists(courseId);
        sectionDomainService.validateSectionBelongsToCourse(sectionDomainService.findSectionBySlug(sectionSlug), courseId);

        List<Lesson> lessons = lessonDomainService.findLessonsBySectionId(sectionDomainService.findSectionBySlug(sectionSlug).getSectionId());
        return lessons.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private LessonResponseDto mapToResponse(Lesson lesson) {
        return LessonResponseDto.builder()
            .lessonId(lesson.getLessonId())
            .lessonSlug(lesson.getLessonSlug() != null ? lesson.getLessonSlug() : SlugUtil.toSlug(lesson.getTitle()))
            .title(lesson.getTitle())
            .sectionId(lesson.getSection().getSectionId())
            .orderIndex(lesson.getOrderIndex())
            .createdAt(lesson.getCreatedAt())
            .updatedAt(lesson.getUpdatedAt())
            .build();
    }
}
