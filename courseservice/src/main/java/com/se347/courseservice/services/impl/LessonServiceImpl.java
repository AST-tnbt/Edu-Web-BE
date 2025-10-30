package com.se347.courseservice.services.impl;

import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.dtos.LessonResponseDto;
import com.se347.courseservice.entities.Lesson;
import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.services.CourseService;
import com.se347.courseservice.services.LessonService;
import com.se347.courseservice.repositories.LessonRepository;

import java.util.List;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;

    private final CourseService courseService;

    public LessonServiceImpl(LessonRepository lessonRepository, CourseService courseService) {
        this.lessonRepository = lessonRepository;
        this.courseService = courseService;
    }

    @Override
    public LessonResponseDto createLesson(LessonRequestDto request) {

        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }

        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }

        if (request.getCourseId() == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }

        if (request.getOrderIndex() <= 0) {
            throw new CourseException.InvalidRequestException("Order index must be greater than 0");
        }
        
        if (lessonRepository.existsByCourseIdAndTitle(request.getCourseId(), request.getTitle())) {
            throw new CourseException.InvalidRequestException("Lesson with title '" + request.getTitle() + "' already exists for course '" + request.getCourseId() + "'");
        }

        if (!courseService.courseExists(request.getCourseId())) {
            throw new CourseException.CourseNotFoundException(request.getCourseId().toString());
        }

        Lesson lesson = Lesson.builder()
            .title(request.getTitle())
            .courseId(request.getCourseId())
            .orderIndex(request.getOrderIndex())
            .build();

        lesson.onCreate();
        lessonRepository.save(lesson);
        return mapToResponse(lesson);
    }

    @Override
    public LessonResponseDto getLessonById(UUID lessonId) {
        if (lessonId == null) {
            throw new CourseException.InvalidRequestException("Lesson ID cannot be null");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new CourseException.LessonNotFoundException(lessonId.toString()));
        return mapToResponse(lesson);
    }

    @Override
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

        if (request.getCourseId() == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }

        if (lessonRepository.existsByCourseIdAndTitle(request.getCourseId(), request.getTitle())) {
            throw new CourseException.InvalidRequestException("Lesson with title '" + request.getTitle() + "' already exists for course '" + request.getCourseId() + "'");
        }

        if (!courseService.courseExists(request.getCourseId())) {
            throw new CourseException.CourseNotFoundException(request.getCourseId().toString());
        }

        Lesson existingLesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new CourseException.LessonNotFoundException(lessonId.toString()));

        existingLesson.setTitle(request.getTitle());
        existingLesson.setCourseId(request.getCourseId());
        existingLesson.setOrderIndex(request.getOrderIndex());
        existingLesson.onUpdate();
        lessonRepository.save(existingLesson);

        return mapToResponse(existingLesson);
    }

    @Override
    public List<LessonResponseDto> getLessonsByCourseId(UUID courseId){

        if (courseId == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }

        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
        return lessons.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private LessonResponseDto mapToResponse(Lesson lesson) {
        return LessonResponseDto.builder()
            .lessonId(lesson.getLessonId())
            .title(lesson.getTitle())
            .courseId(lesson.getCourseId())
            .orderIndex(lesson.getOrderIndex())
            .createdAt(lesson.getCreatedAt())
            .updatedAt(lesson.getUpdatedAt())
            .build();
    }
}
