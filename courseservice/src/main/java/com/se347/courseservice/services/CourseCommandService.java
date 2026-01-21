package com.se347.courseservice.services;

import com.se347.courseservice.dtos.CourseRequestDto;
import com.se347.courseservice.dtos.CourseResponseDto;
import com.se347.courseservice.dtos.SectionRequestDto;
import com.se347.courseservice.dtos.SectionResponseDto;
import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.dtos.LessonResponseDto;
import com.se347.courseservice.dtos.ContentMetadataRequestDto;
import com.se347.courseservice.dtos.ContentMetadataResponseDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface CourseCommandService {

    /*
    Course
     */
    CourseResponseDto createCourse(CourseRequestDto request, MultipartFile thumbnail, UUID userId);
    CourseResponseDto updateCourseById(UUID courseId, CourseRequestDto request, UUID userId);
    CourseResponseDto updateCourseByCourseSlug(String courseSlug, CourseRequestDto request, UUID userId);

    /*
    Section
     */
    SectionResponseDto createSection(UUID courseId, SectionRequestDto request, UUID userId);
    SectionResponseDto updateSectionById(UUID courseId, UUID sectionId, SectionRequestDto request, UUID userId);
    SectionResponseDto updateSectionBySectionSlug(String courseSlug, String sectionSlug, SectionRequestDto request, UUID userId);

    /*
    Lesson
     */
    LessonResponseDto createLesson(UUID courseId, UUID sectionId, LessonRequestDto request, UUID userId);
    LessonResponseDto updateLessonById(UUID courseId, UUID sectionId, UUID lessonId, LessonRequestDto request, UUID userId);
    LessonResponseDto updateLessonByLessonSlug(String courseSlug, String sectionSlug, String lessonSlug, LessonRequestDto request, UUID userId);

    /*
    Content
     */
    ContentMetadataResponseDto createContent(UUID courseId, UUID sectionId, UUID lessonId, ContentMetadataRequestDto request, UUID userId);
    ContentMetadataResponseDto updateContentById(UUID courseId, UUID sectionId, UUID lessonId, UUID contentId, ContentMetadataRequestDto request, UUID userId);
}
