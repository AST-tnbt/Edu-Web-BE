package com.se347.courseservice.services.impl;

import com.se347.courseservice.domains.CourseDomainService;
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
import com.se347.courseservice.repositories.LessonRepository;
import com.se347.courseservice.clients.EnrollmentServiceClient;
import com.se347.courseservice.publishers.CoursePublisher;
import com.se347.courseservice.dtos.events.setTotalLessonsEventDto;
import com.se347.courseservice.enums.ContentType;
import com.se347.courseservice.entities.valueobjects.OrderIndex;
import com.se347.courseservice.exceptions.ContentMetadataException;
import org.springframework.stereotype.Service;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application Service for Course operations
 * 
 * RESPONSIBILITIES (DDD Application Layer):
 * 1. Transaction boundaries (@Transactional)
 * 2. Orchestration: coordinate domain objects and services
 * 3. DTO mapping: Entity ↔ DTO conversion
 * 4. Cross-aggregate coordination via Domain Service
 * 
 * DOES NOT CONTAIN:
 * - Business logic (moved to Course entity)
 * - Validation (moved to Course entity guards)
 * - Entity creation logic (moved to Course.createNew())
 * 
 * This is THIN orchestration layer as DDD intended!
 */
@RequiredArgsConstructor
@Service
public class CourseCommandServiceImpl implements CourseCommandService {

    private final CourseRepository courseRepository;
    private final CourseDomainService courseDomainService;
    private final LessonRepository lessonRepository;
    private final EnrollmentServiceClient enrollmentServiceClient;
    private final CoursePublisher coursePublisher;
    private final Logger logger = LoggerFactory.getLogger(CourseCommandServiceImpl.class);

    /**
     * Create a new course
     * 
     * DDD Flow:
     * 1. Cross-aggregate check (Course ↔ Category) via Domain Service
     * 2. Check title uniqueness via Domain Service
     * 3. Tell Course aggregate to create itself (all validation inside)
     * 4. Save aggregate root
     * 5. Map to DTO
     */
    @Transactional
    @Override
    public CourseResponseDto createCourse(CourseRequestDto request, UUID userId) {
        // 1. Cross-aggregate validation: ensure category exists
        courseDomainService.ensureCategoryExists(request.getCategoryName());
        
        // 2. Business rule: course title must be unique
        if (!courseDomainService.isTitleUnique(request.getTitle())) {
            throw new CourseException.CourseAlreadyExistsException(request.getTitle());
        }
        
        // 3. Tell aggregate to create itself (factory method with all validations)
        Money price = Money.of(request.getPrice());
        Course course = Course.createNew(
            request.getTitle(),
            request.getDescription(),
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

    /**
     * Update course by ID
     * 
     * DDD Flow:
     * 1. Load aggregate root
     * 2. Authorization check (aggregate self-validates)
     * 3. Cross-aggregate check (Category)
     * 4. Title uniqueness check (if changed)
     * 5. Tell aggregate to update itself
     * 6. Save aggregate root
     */
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

    /**
     * Update course by slug
     * Same logic as updateById, but find by slug first
     */
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

    /**
     * Create a new section in a course
     * 
     * DDD Flow:
     * 1. Load Course aggregate root
     * 2. Authorization check
     * 3. Tell Course aggregate to add section (all validation inside)
     * 4. Save Course aggregate root (cascade saves section)
     */
    @Override
    @Transactional
    public SectionResponseDto createSection(UUID courseId, SectionRequestDto request, UUID userId) {
        // 1. Load aggregate root (eager-load sections for consistency)
        Course course = courseRepository.findByIdWithSections(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // 2. Authorization: ensure user owns this course
        course.ensureOwnedBy(userId);
        
        // 3. Tell aggregate to add section (validation inside Course.addSection())
        Section section = course.addSection(
            request.getTitle(),
            request.getDescription(),
            request.getOrderIndex()
        );
        
        // 4. Save aggregate root (JPA cascade will save section)
        courseRepository.save(course);
        
        return mapToResponse(section);
    }

    /**
     * Update section by ID
     * 
     * DDD Flow:
     * 1. Load Course aggregate (to maintain consistency)
     * 2. Find section within aggregate
     * 3. Authorization check
     * 4. Tell section to update itself
     * 5. Save aggregate root
     */
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

    /**
     * Update section by slug
     */
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
            .filter(s -> s.getSectionSlug().getValue().equals(sectionSlug))
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

    /**
     * Create a new lesson in a section
     * 
     * DDD Flow:
     * 1. Load Course aggregate
     * 2. Find Section within aggregate
     * 3. Tell Section to add lesson (validation inside)
     * 4. Save Course aggregate
     * 5. Publish event if there are enrollments
     */
    @Override
    @Transactional
    public LessonResponseDto createLesson(UUID courseId, UUID sectionId, LessonRequestDto request, UUID userId) {
        // 1. Load Course aggregate (with sections eager-loaded)
        Course course = courseRepository.findByIdWithSections(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // 2. Authorization check
        course.ensureOwnedBy(userId);

        // 2. Find Section within aggregate
        Section section = course.getSections().stream()
            .filter(s -> s.getSectionId().equals(sectionId))
            .findFirst()
            .orElseThrow(() -> new CourseException.SectionNotFoundException(sectionId.toString()));
        
        // 3. Tell Section to add lesson (all validation inside Section.addLesson())
        Lesson lesson = section.addLesson(
            request.getTitle(),
            request.getOrderIndex()
        );
        
        // 4. Save Course aggregate (cascade saves lesson)
        courseRepository.save(course);
        
        // 5. Publish event for enrollment service (side effect)
        publishTotalLessonsEventIfNeeded(courseId, course);
        
        return mapToResponse(lesson);
    }

    /**
     * Update lesson by slug
     */
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
        
        // Find section and lesson
        Section section = course.getSections().stream()
            .filter(s -> s.getSectionSlug().getValue().equals(sectionSlug))
            .findFirst()
            .orElseThrow(() -> new CourseException.SectionNotFoundException("Section with slug '" + sectionSlug + "' not found"));
        
        Lesson lesson = section.getLessons().stream()
            .filter(l -> l.getLessonSlug().getValue().equals(lessonSlug))
            .findFirst()
            .orElseThrow(() -> new CourseException.LessonNotFoundException("Lesson with slug '" + lessonSlug + "' not found"));
        
        // Update
        lesson.updateDetails(request.getTitle(), request.getOrderIndex());
        
        // Save aggregate
        courseRepository.save(course);
        
        return mapToResponse(lesson);
    }

    /**
     * Update lesson by ID
     */
    @Override
    @Transactional
    public LessonResponseDto updateLessonById(UUID courseId, UUID sectionId, UUID lessonId, LessonRequestDto request, UUID userId) {
        // Load Course aggregate
        Course course = courseRepository.findByIdWithSections(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // Authorization
        course.ensureOwnedBy(userId);
        
        // Find section
        Section section = course.getSections().stream()
            .filter(s -> s.getSectionId().equals(sectionId))
            .findFirst()
            .orElseThrow(() -> new CourseException.SectionNotFoundException(sectionId.toString()));
        
        // Find lesson
        Lesson lesson = section.getLessons().stream()
            .filter(l -> l.getLessonId().equals(lessonId))
            .findFirst()
            .orElseThrow(() -> new CourseException.LessonNotFoundException(lessonId.toString()));
        
        // Update
        lesson.updateDetails(request.getTitle(), request.getOrderIndex());
        
        // Save aggregate
        courseRepository.save(course);
        
        return mapToResponse(lesson);
    }

    /**
     * Create new content in a lesson
     * 
     * DDD Strategy:
     * 1. Load Lesson aggregate (with contents for validation)
     * 2. Authorization via lesson.ensureOwnedBy() → delegates to Course
     * 3. Tell Lesson to add Content (domain method enforces invariants)
     * 4. Save Lesson aggregate (cascades to Content)
     * 
     * Why save Lesson, not Content?
     * → Respects aggregate boundary
     * → Lesson is the transaction boundary
     * → Cascade will persist Content automatically
     */
    @Override
    @Transactional
    public ContentMetadataResponseDto createContent(UUID courseId, UUID sectionId, UUID lessonId, ContentMetadataRequestDto request, UUID userId) {
        
        // 1. Load Course aggregate
        Course course = courseRepository.findByIdWithSections(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // 2. Authorization check
        course.ensureOwnedBy(userId);

        // 3. Find Section within aggregate
        Section section = course.getSections().stream()
            .filter(s -> s.getSectionId().equals(sectionId))
            .findFirst()
            .orElseThrow(() -> new CourseException.SectionNotFoundException(sectionId.toString()));

        // 4. Find Lesson within aggregate
        Lesson lesson = section.getLessons().stream()
            .filter(l -> l.getLessonId().equals(lessonId))
            .findFirst()
            .orElseThrow(() -> new CourseException.LessonNotFoundException(lessonId.toString()));
        
        // 3. Validate orderIndex (Value Object handles validation)
        OrderIndex orderIndex = OrderIndex.of(request.getOrderIndex());
        
        // 4. Tell Lesson to add Content (domain method)
        // This enforces invariants (e.g., no duplicate content titles)
        Content content = lesson.addContent(
            ContentType.valueOf(request.getContentType().name()),
            request.getTitle(),
            request.getContentUrl(),
            request.getTextContent(),
            orderIndex.getValue() // Extract primitive value
        );
        
        // 5. Save Lesson aggregate (cascades to Content via CascadeType.ALL)
        lessonRepository.save(lesson);
        
        return mapToResponse(content);
    }

    /**
     * Update content
     * 
     * DDD Strategy:
     * 1. Load Lesson aggregate (with contents)
     * 2. Authorization check
     * 3. Find content within aggregate
     * 4. Update via content.updateDetails()
     * 5. Save Lesson aggregate
     */
    @Override
    @Transactional
    public ContentMetadataResponseDto updateContentById(UUID courseId, UUID sectionId, UUID lessonId, UUID contentId, ContentMetadataRequestDto request, UUID userId) {
        
        // 1. Load Course aggregate
        Course course = courseRepository.findByIdWithSections(courseId)
            .orElseThrow(() -> new CourseException.CourseNotFoundException(courseId.toString()));
        
        // 2. Authorization check
        course.ensureOwnedBy(userId);
        
        // 3. Find Section within aggregate
        Section section = course.getSections().stream()
            .filter(s -> s.getSectionId().equals(sectionId))
            .findFirst()
            .orElseThrow(() -> new CourseException.SectionNotFoundException(sectionId.toString()));
        
        // 4. Find Lesson within aggregate
        Lesson lesson = section.getLessons().stream()
            .filter(l -> l.getLessonId().equals(lessonId))
            .findFirst()
            .orElseThrow(() -> new CourseException.LessonNotFoundException(lessonId.toString()));
        
        // 3. Find content within the aggregate
        Content content = lesson.getContents().stream()
            .filter(c -> c.getContentId().equals(contentId))
            .findFirst()
            .orElseThrow(() -> new ContentMetadataException.ContentNotFoundException(
                "Content " + contentId + " not found in lesson " + lessonId
            ));
        
        // 4. Validate orderIndex (Value Object handles validation)
        OrderIndex orderIndex = OrderIndex.of(request.getOrderIndex());
        
        // 5. Update content using its domain method
        content.updateDetails(
            request.getTitle(),
            request.getContentUrl(),
            request.getTextContent(),
            orderIndex.getValue() // Extract primitive value
        );
        
        // 6. Save Lesson aggregate (cascades to Content)
        lessonRepository.save(lesson);
        
        return mapToResponse(content);
    }

    /**
     * Publish content (make it available to students)
     * 
     * DDD Strategy (Pragmatic Approach):
     * 1. Load Lesson aggregate (with contents)
     * 2. Authorization via lesson.ensureOwnedBy() → delegates to Course
     * 3. Find content within aggregate
     * 4. Execute domain method: content.publish()
     * 5. Save Lesson aggregate (cascades to Content)
     * 
     * Why load Lesson instead of full Course aggregate?
     * - Publish is self-contained state transition
     * - No aggregate-level invariants to enforce
     * - Performance optimization (don't need full Course)
     * - Still respects aggregate boundaries (Lesson controls its Contents)
     */
    @Override
    @Transactional
    public ContentMetadataResponseDto publishContent(UUID lessonId, UUID contentId, UUID userId) {
        // 1. Load Lesson aggregate with contents
        Lesson lesson = lessonRepository.findByIdWithContents(lessonId)
            .orElseThrow(() -> new CourseException.LessonNotFoundException(lessonId.toString()));
        
        // 2. Authorization check (delegates to Course)
        lesson.ensureOwnedBy(userId);
        
        // 3. Find content within aggregate
        Content content = lesson.getContents().stream()
            .filter(c -> c.getContentId().equals(contentId))
            .findFirst()
            .orElseThrow(() -> new ContentMetadataException.ContentNotFoundException(
                "Content " + contentId + " not found in lesson " + lessonId
            ));
        
        // 4. Publish using domain method (enforces business rules)
        content.publish();
        
        // 5. Save Lesson aggregate (cascades to Content)
        lessonRepository.save(lesson);
        
        return mapToResponse(content);
    }

    /**
     * Unpublish content (back to draft)
     * 
     * Same strategy as publish
     */
    @Override
    @Transactional
    public ContentMetadataResponseDto unpublishContent(UUID lessonId, UUID contentId, UUID userId) {
        // 1. Load Lesson aggregate with contents
        Lesson lesson = lessonRepository.findByIdWithContents(lessonId)
            .orElseThrow(() -> new CourseException.LessonNotFoundException(lessonId.toString()));
        
        // 2. Authorization check
        lesson.ensureOwnedBy(userId);
        
        // 3. Find content within aggregate
        Content content = lesson.getContents().stream()
            .filter(c -> c.getContentId().equals(contentId))
            .findFirst()
            .orElseThrow(() -> new ContentMetadataException.ContentNotFoundException(
                "Content " + contentId + " not found in lesson " + lessonId
            ));
        
        // 4. Unpublish using domain method
        content.unpublish();
        
        // 5. Save Lesson aggregate
        lessonRepository.save(lesson);
        
        return mapToResponse(content);
    }

    /**
     * Map Course entity to DTO
     * 
     * IMPORTANT: Value Objects need .getValue() or .getAmount() to extract primitives
     */
    private CourseResponseDto mapToResponse(Course course) {
        return CourseResponseDto.builder()
            .courseId(course.getCourseId())
            .courseSlug(course.getCourseSlug().getValue()) // ← Value Object: need .getValue()
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
            .sectionSlug(section.getSectionSlug().getValue()) // ← Value Object: need .getValue()
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
            .lessonSlug(lesson.getLessonSlug().getValue()) // ← Value Object
            .title(lesson.getTitle())
            .sectionId(lesson.getSection().getSectionId())
            .orderIndex(lesson.getOrderIndex().getValue()) // ← Value Object
            .createdAt(lesson.getCreatedAt())
            .updatedAt(lesson.getUpdatedAt())
            .build();
    }

    /**
     * Publish event to enrollment service when total lessons change
     * (Side effect - not core domain logic)
     */
    private void publishTotalLessonsEventIfNeeded(UUID courseId, Course course) {
        try {
            if (!enrollmentServiceClient.getEnrollmentsbyCourseId(courseId).isEmpty()) {
                Integer totalLessons = course.getTotalLessonsCount(); // Domain method!
                
                setTotalLessonsEventDto event = setTotalLessonsEventDto.builder()
                    .courseId(courseId)
                    .totalLessons(totalLessons)
                    .build();
                
                coursePublisher.publishSetTotalLessonsEvent(event);
            }
        } catch (Exception e) {
            logger.warn("Failed to publish total lessons event for course: {}", courseId, e);
            // Don't throw - this is a side effect, not critical
        }
    }

    private ContentMetadataResponseDto mapToResponse(Content content) {
        return ContentMetadataResponseDto.builder()
            .contentId(content.getContentId())
            .lessonId(content.getLesson().getLessonId())
            .contentType(content.getType())
            .title(content.getTitle())
            .contentUrl(content.getContentUrl())
            .textContent(content.getTextContent())
            .orderIndex(content.getOrderIndex().getValue()) // Extract from Value Object
            .status(content.getStatus())
            .createdAt(content.getCreatedAt())
            .updatedAt(content.getUpdatedAt())
            .build();
    }
}
