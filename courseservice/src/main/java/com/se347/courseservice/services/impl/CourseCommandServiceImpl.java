package com.se347.courseservice.services.impl;

import com.se347.courseservice.domains.CourseDomainService;
import com.se347.courseservice.domains.SlugGenerateService;
import com.se347.courseservice.dtos.CourseRequestDto;
import com.se347.courseservice.dtos.CourseResponseDto;
import com.se347.courseservice.services.CourseCommandService;
import com.se347.courseservice.repositories.CourseRepository;
import com.se347.courseservice.entities.Course;
import com.se347.courseservice.entities.Section;
import com.se347.courseservice.dtos.SectionRequestDto;
import com.se347.courseservice.dtos.SectionResponseDto;
import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.dtos.LessonResponseDto;
import com.se347.courseservice.entities.Lesson;
import com.se347.courseservice.dtos.ContentMetadataRequestDto;
import com.se347.courseservice.dtos.ContentMetadataResponseDto;
import com.se347.courseservice.entities.Content;
import com.se347.courseservice.entities.valueobjects.Money;
import com.se347.courseservice.exceptions.CourseException;
import org.springframework.stereotype.Service;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CourseCommandServiceImpl implements CourseCommandService {

    private final CourseRepository courseRepository;
    private final CourseDomainService courseDomainService;
    private final SlugGenerateService slugGenerateService;
    @Transactional
    @Override
    public CourseResponseDto createCourse(CourseRequestDto request, UUID userId) {
        // 1. Cross-aggregate validation: ensure category exists
        courseDomainService.ensureCategoryExists(request.getCategoryName());
        
        // 2. Business rule: course title must be unique
        if (!courseDomainService.isTitleUnique(request.getTitle())) {
            throw new CourseException.CourseAlreadyExistsException(request.getTitle());
        }
        
        // 3. Generate unique slug for course
        String courseSlug = slugGenerateService.generateCourseSlug(request.getTitle());

        // 3. Tell aggregate to create itself (factory method with all validations)
        Money price = Money.of(request.getPrice());
        Course course = Course.createNew(
            request.getTitle(),
            request.getDescription(),
            courseSlug,
            request.getThumbnailUrl(),
            price,
            request.getLevel(),
            request.getCategoryName(),
            userId
        );
        
        // 4. Save aggregate root (will publish domain events automatically)
        courseRepository.save(course);

        // 5. Map to DTO for presentation layer
        return mapToResponse(course);
    }

    @Transactional
    @Override
    public CourseResponseDto updateCourseById(UUID courseId, CourseRequestDto request, UUID userId) {
        // 1. Load aggregate root
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // 2. Authorization: ensure user owns this course
        course.ensureOwnedBy(userId);
        
        // 3. Cross-aggregate validation: ensure category exists
        courseDomainService.ensureCategoryExists(request.getCategoryName());
        
        // 4. Business rule: title must be unique (if changing)
        if (!course.getTitle().equals(request.getTitle())) {
            if (!courseDomainService.isTitleUniqueExcluding(request.getTitle(), courseId)) {
                throw new CourseException.CourseAlreadyExistsException(request.getTitle());
            }
        }
        
        // 5. Tell aggregate to update itself (all validation inside)
        Money price = Money.of(request.getPrice());
        course.updateDetails(
            request.getTitle(),
            request.getDescription(),
            request.getThumbnailUrl(),
            price,
            request.getLevel()
        );
        
        // 6. Save aggregate root (will publish CourseUpdatedEvent)
        courseRepository.save(course);
        
        return mapToResponse(course);
    }

    @Transactional
    @Override
    public CourseResponseDto updateCourseByCourseSlug(String courseSlug, CourseRequestDto request, UUID userId) {
        // Find by slug (using Value Object field)
        Course course = courseRepository.findByCourseSlug(courseSlug)
            .orElseThrow(() -> new CourseException.CourseNotFoundException("Course with slug '" + courseSlug + "' not found"));
        
        // Rest is same as updateById
        course.ensureOwnedBy(userId);
        courseDomainService.ensureCategoryExists(request.getCategoryName());
        
        if (!course.getTitle().equals(request.getTitle())) {
            if (!courseDomainService.isTitleUniqueExcluding(request.getTitle(), course.getCourseId())) {
                throw new CourseException.CourseAlreadyExistsException(request.getTitle());
            }
        }
        
        Money price = Money.of(request.getPrice());
        course.updateDetails(
            request.getTitle(),
            request.getDescription(),
            request.getThumbnailUrl(),
            price,
            request.getLevel()
        );
        
        courseRepository.save(course);
        return mapToResponse(course);
    }

    @Override
    @Transactional
    public SectionResponseDto createSection(UUID courseId, SectionRequestDto request, UUID userId) {
        // 1. Load aggregate root (eager-load sections for consistency)
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // 2. Authorization: ensure user owns this course
        course.ensureOwnedBy(userId);
        
        // 3. Generate unique slug for section
        String sectionSlug = slugGenerateService.generateSectionSlug(request.getTitle());

        // 3. Tell aggregate to add section (validation inside Course.addSection())
        Section section = course.addSection(
            request.getTitle(),
            request.getDescription(),
            sectionSlug,
            request.getOrderIndex()
        );
        
        // 4. Save aggregate root (JPA cascade will save section)
        courseRepository.save(course);
        
        return mapToResponse(section);
    }

    @Override
    @Transactional
    public SectionResponseDto updateSectionById(UUID courseId, UUID sectionId, SectionRequestDto request, UUID userId) {
        // 1. Load Course aggregate with sections
        Course course = courseRepository.findByIdWithSections(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // 2. Authorization: ensure user owns course
        course.ensureOwnedBy(userId);
        
        // 3. Find section within aggregate
        Section section = course.getSections().stream()
            .filter(s -> s.getSectionId().equals(sectionId))
            .findFirst()
            .orElseThrow(() -> new CourseException.SectionNotFoundException(sectionId.toString()));
        
        // 4. Tell section to update itself (validation inside)
        section.updateDetails(
            request.getTitle(),
            request.getDescription(),
            request.getOrderIndex()
        );
        
        // 5. Save aggregate root (JPA cascade updates section)
        courseRepository.save(course);
        
        return mapToResponse(section);
    }

    @Override
    @Transactional
    public SectionResponseDto updateSectionBySectionSlug(String courseSlug, String sectionSlug, SectionRequestDto request, UUID userId) {
        // Load Course by slug
        Course courseBySlug = courseRepository.findByCourseSlug(courseSlug)
            .orElseThrow(() -> new CourseException.CourseNotFoundException("Course with slug '" + courseSlug + "' not found"));
        
        // Load with sections
        Course course = courseRepository.findByIdWithSections(courseBySlug.getCourseId())
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseBySlug.getCourseId().toString()));
        
        // Authorization
        course.ensureOwnedBy(userId);
        
        // Find section by slug within aggregate
        Section section = course.getSections().stream()
            .filter(s -> s.getSectionSlug().equals(sectionSlug))
            .findFirst()
            .orElseThrow(() -> new CourseException.SectionNotFoundException("Section with slug '" + sectionSlug + "' not found"));
        
        // Update
        section.updateDetails(
            request.getTitle(),
            request.getDescription(),
            request.getOrderIndex()
        );
        
        courseRepository.save(course);
        
        return mapToResponse(section);
    }

    @Override
    @Transactional
    public LessonResponseDto createLesson(UUID courseId, UUID sectionId, LessonRequestDto request, UUID userId) {
        // 1. Load Course aggregate (with sections eager-loaded)
        Course course = courseRepository.findByIdWithSections(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // 2. Authorization check
        course.ensureOwnedBy(userId);

        // 3. Generate unique slug for lesson
        String lessonSlug = slugGenerateService.generateLessonSlug(request.getTitle());

        Lesson lesson = course.addLessonToSection(sectionId, request.getTitle(), lessonSlug, request.getOrderIndex());
        
        // 4. Save Course aggregate (cascade saves lesson)
        courseRepository.save(course);
        
        return mapToResponse(lesson);
    }

    @Transactional
    @Override
    public LessonResponseDto updateLessonByLessonSlug(String courseSlug, String sectionSlug, String lessonSlug, LessonRequestDto request, UUID userId) {
        // Load Course
        Course courseBySlug = courseRepository.findByCourseSlug(courseSlug)
            .orElseThrow(() -> new CourseException.CourseNotFoundException("Course with slug '" + courseSlug + "' not found"));
        
        // Authorization
        courseBySlug.ensureOwnedBy(userId);
        
        // Load with full aggregate
        Course course = courseRepository.findByIdWithSections(courseBySlug.getCourseId())
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseBySlug.getCourseId().toString()));
        
        Lesson lesson = course.updateLessonInSectionSlug(
                sectionSlug, 
                lessonSlug, 
                request.getTitle(), 
                request.getOrderIndex());
        
        // Save aggregate
        courseRepository.save(course);
        
        return mapToResponse(lesson);
    }

    @Override
    @Transactional
    public LessonResponseDto updateLessonById(UUID courseId, UUID sectionId, UUID lessonId, LessonRequestDto request, UUID userId) {
        // Load Course aggregate
        Course course = courseRepository.findByIdWithSections(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // Authorization
        course.ensureOwnedBy(userId);
        
        Lesson lesson = course.updateLessonInSection(
                sectionId, 
                lessonId, 
                request.getTitle(), 
                request.getOrderIndex());

        courseRepository.save(course);
        
        return mapToResponse(lesson);
    }

    @Override
    @Transactional
    public ContentMetadataResponseDto createContent(UUID courseId, UUID sectionId, UUID lessonId, ContentMetadataRequestDto request, UUID userId) {
        
        // 1. Load Course aggregate
        Course course = courseRepository.findByIdWithSections(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // 2. Authorization check
        course.ensureOwnedBy(userId);

        Content content = course.addContentToLesson(
                sectionId, 
                lessonId, 
                request.getContentUrl(), 
                request.getOrderIndex());
        
        courseRepository.save(course);
        
        return mapToResponse(content);
    }

    @Override
    @Transactional
    public ContentMetadataResponseDto updateContentById(UUID courseId, UUID sectionId, UUID lessonId, UUID contentId, ContentMetadataRequestDto request, UUID userId) {
        
        // 1. Load Course aggregate
        Course course = courseRepository.findByIdWithSections(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // 2. Authorization check
        course.ensureOwnedBy(userId);
            
        Content content = course.updateContentByLessonId(
                sectionId, 
                lessonId, 
                contentId, 
                request.getContentUrl(), 
                request.getOrderIndex());
        
        courseRepository.save(course);
        
        return mapToResponse(content);
    }


    private CourseResponseDto mapToResponse(Course course) {
        return CourseResponseDto.builder()
            .courseId(course.getCourseId())
            .courseSlug(course.getCourseSlug())
            .title(course.getTitle())
            .description(course.getDescription())
            .thumbnailUrl(course.getThumbnailUrl())
            .price(course.getPrice().getAmount()) // ← Value Object: need .getAmount()
            .level(course.getLevel())
            .categoryName(course.getCategoryName())
            .instructorId(course.getInstructorId())
            .createdAt(course.getCreatedAt())
            .updatedAt(course.getUpdatedAt())
            .build();
    }

    private SectionResponseDto mapToResponse(Section section) {
        return SectionResponseDto.builder()
            .sectionId(section.getSectionId())
            .sectionSlug(section.getSectionSlug())
            .courseId(section.getCourse().getCourseId())
            .title(section.getTitle())
            .description(section.getDescription())
            .orderIndex(section.getOrderIndex().getValue()) // ← Value Object: need .getValue()
            .createdAt(section.getCreatedAt())
            .updatedAt(section.getUpdatedAt())
            .build();
    }

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

    private ContentMetadataResponseDto mapToResponse(Content content) {
        return ContentMetadataResponseDto.builder()
            .contentId(content.getContentId())
            .lessonId(content.getLesson().getLessonId())
            .contentUrl(content.getContentUrl())
            .orderIndex(content.getOrderIndex().getValue()) // Extract from Value Object
            .createdAt(content.getCreatedAt())
            .updatedAt(content.getUpdatedAt())
            .build();
    }
}
