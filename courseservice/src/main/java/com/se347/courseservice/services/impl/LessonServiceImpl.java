package com.se347.courseservice.services.impl;

import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.dtos.LessonResponseDto;
import com.se347.courseservice.entities.Lesson;
import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.services.SectionService;
import com.se347.courseservice.services.LessonService;
import com.se347.courseservice.repositories.LessonRepository;
import com.se347.courseservice.clients.EnrollmentServiceClient;
import com.se347.courseservice.publishers.CoursePublisher;
import com.se347.courseservice.dtos.events.setTotalLessonsEventDto;
import com.se347.courseservice.services.CourseService;
import com.se347.courseservice.entities.Section;

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
    private final SectionService sectionService;
    private final EnrollmentServiceClient enrollmentServiceClient;
    private final CoursePublisher coursePublisher;
    private final CourseService courseService;
    private final Logger logger = LoggerFactory.getLogger(LessonServiceImpl.class);
    
    @Override
    @Transactional
    public LessonResponseDto createLesson(LessonRequestDto request) {

        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }

        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }

        if (request.getSectionId() == null) {
            throw new CourseException.InvalidRequestException("Section ID cannot be null");
        }

        if (request.getOrderIndex() <= 0) {
            throw new CourseException.InvalidRequestException("Order index must be greater than 0");
        }
        
        String normalizedTitle = request.getTitle().trim();

        if (lessonRepository.existsBySectionIdAndTitle(request.getSectionId(), normalizedTitle)) {
            throw new CourseException.LessonAlreadyExistsException(request.getSectionId().toString(), normalizedTitle);
        }

        if (!sectionService.sectionExists(request.getSectionId())) {
            throw new CourseException.SectionNotFoundException(request.getSectionId().toString());
        }

        UUID sectionId = request.getSectionId();
        Section section = sectionService.toSection(sectionId); // Lấy 1 lần
        UUID courseId = section.getCourse().getCourseId();

        Lesson lesson = Lesson.builder()
            .title(normalizedTitle)
            .section(section) // Dùng section đã lấy
            .orderIndex(request.getOrderIndex())
            .build();
        lesson.onCreate();
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
    public LessonResponseDto getLessonById(UUID lessonId) {
        if (lessonId == null) {
            throw new CourseException.InvalidRequestException("Lesson ID cannot be null");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new CourseException.LessonNotFoundException(lessonId.toString()));
        return mapToResponse(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public Lesson toLesson(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new CourseException.LessonNotFoundException(lessonId.toString()));
        return lesson;
    }

    @Override
    @Transactional
    public LessonResponseDto updateLesson(UUID lessonId, LessonRequestDto request) {

        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }

        if (lessonId == null) {
            throw new CourseException.InvalidRequestException("Lesson ID cannot be null");
        }

        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }

        if (request.getSectionId() == null) {
            throw new CourseException.InvalidRequestException("Section ID cannot be null");
        }

        String normalizedTitle = request.getTitle().trim();

        if (!sectionService.sectionExists(request.getSectionId())) {
            throw new CourseException.SectionNotFoundException(request.getSectionId().toString());
        }

        Lesson existingLesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new CourseException.LessonNotFoundException(lessonId.toString()));

        // Only check duplicate if title or section changes
        boolean titleChanged = !normalizedTitle.equals(existingLesson.getTitle());
        boolean sectionChanged = !request.getSectionId().equals(existingLesson.getSection().getSectionId());
        if ((titleChanged || sectionChanged) && lessonRepository.existsBySectionIdAndTitle(request.getSectionId(), normalizedTitle)) {
            throw new CourseException.LessonAlreadyExistsException(request.getSectionId().toString(), normalizedTitle);
        }

        existingLesson.setTitle(normalizedTitle);
        existingLesson.setSection(sectionService.toSection(request.getSectionId()));
        existingLesson.setOrderIndex(request.getOrderIndex());
        existingLesson.onUpdate();
        lessonRepository.save(existingLesson);

        return mapToResponse(existingLesson);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonResponseDto> getLessonsBySectionId(UUID sectionId){

        if (sectionId == null) {
            throw new CourseException.InvalidRequestException("Section ID cannot be null");
        }

        List<Lesson> lessons = lessonRepository.findBySectionId(sectionId);
        return lessons.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private LessonResponseDto mapToResponse(Lesson lesson) {
        return LessonResponseDto.builder()
            .lessonId(lesson.getLessonId())
            .title(lesson.getTitle())
            .sectionId(lesson.getSection().getSectionId())
            .orderIndex(lesson.getOrderIndex())
            .createdAt(lesson.getCreatedAt())
            .updatedAt(lesson.getUpdatedAt())
            .build();
    }
}
