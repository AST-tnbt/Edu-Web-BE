package com.se347.courseservice.entities;

import com.se347.courseservice.enums.CourseLevel;
import com.se347.courseservice.entities.valueobjects.Money;
import com.se347.courseservice.entities.valueobjects.Slug;
import com.se347.courseservice.domains.events.CourseCreatedEvent;
import com.se347.courseservice.domains.events.CourseUpdatedEvent;
import com.se347.courseservice.domains.events.SectionAddedToCourseEvent;
import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.exceptions.SectionException;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Getter
@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends AbstractAggregateRoot<Course> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID courseId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "course_slug", unique = true, nullable = false))
    })
    private Slug courseSlug;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column
    private String thumbnailUrl;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price", precision = 10, scale = 2, nullable = false))
    })
    private Money price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CourseLevel level;

    @Column(nullable = false)
    private String categoryName;

    @Column(nullable = false)
    private UUID instructorId;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, updatable = true)
    private LocalDateTime updatedAt;

    /**
     * Factory method to create a new Course
     * 
     * Business rules enforced:
     * - Title, description, category cannot be empty
     * - Price must be positive
     * - Level must be specified
     * 
     * @return new Course instance with CourseCreatedEvent registered
     */
    public static Course createNew(
        String title,
        String description,
        String thumbnailUrl,
        Money price,
        CourseLevel level,
        String categoryName,
        UUID instructorId
    ) {
        // Validation (Guards)
        guardAgainstNullOrEmpty(title, "Title");
        guardAgainstNullOrEmpty(description, "Description");
        guardAgainstNull(price, "Price");
        guardAgainstNull(level, "Level");
        guardAgainstNullOrEmpty(categoryName, "Category");
        guardAgainstNull(instructorId, "Instructor ID");
        
        // Create instance
        Course course = new Course();
        course.courseId = UUID.randomUUID();
        course.courseSlug = Slug.fromTitle(title);
        course.title = title;
        course.description = description;
        course.thumbnailUrl = thumbnailUrl;
        course.price = price;
        course.level = level;
        course.categoryName = categoryName;
        course.instructorId = instructorId;
        course.sections = new ArrayList<>();
        course.createdAt = LocalDateTime.now();
        course.updatedAt = LocalDateTime.now();
        
        // Register domain event
        course.registerEvent(CourseCreatedEvent.from(
            course.courseId,
            course.title,
            course.courseSlug.getValue(),
            course.instructorId,
            course.price.getAmount(),
            course.categoryName
        ));
        
        return course;
    }

    /**
     * Update course details
     * 
     * Business rules:
     * - Title cannot be empty
     * - Price must be positive
     * - Slug is regenerated from new title
     * 
     * Fires: CourseUpdatedEvent
     */
    public void updateDetails(
        String title,
        String description,
        String thumbnailUrl,
        Money price,
        CourseLevel level
    ) {
        // Guards
        guardAgainstNullOrEmpty(title, "Title");
        guardAgainstNullOrEmpty(description, "Description");
        guardAgainstNull(price, "Price");
        guardAgainstNull(level, "Level");
        
        // Update state
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
        this.level = level;
        this.courseSlug = Slug.fromTitle(title); // Regenerate slug
        this.updatedAt = LocalDateTime.now();
        
        // Register event
        registerEvent(CourseUpdatedEvent.from(this.courseId, this.title));
    }

    /**
     * Add a new section to this course
     * 
     * Business rules:
     * - Section title cannot be empty
     * - Section title must be unique within course
     * - Order index must be valid
     * 
     * Enforces aggregate boundary: Section can only be created through Course
     * 
     * @return newly created Section
     * @throws SectionInvariantViolationException if duplicate title
     */
    public Section addSection(String title, String description, int orderIndex) {
        // Guards
        guardAgainstNullOrEmpty(title, "Section title");
        guardAgainstDuplicateSectionTitle(title);
        
        // Create section through aggregate
        Section section = Section.createNew(title, description, orderIndex, this);
        this.sections.add(section);
        this.updatedAt = LocalDateTime.now();
        
        // Register event
        registerEvent(SectionAddedToCourseEvent.from(
            this.courseId,
            section.getSectionId(),
            section.getTitle()
        ));
        
        return section;
    }

    /**
     * Remove a section from course
     */
    public void removeSection(UUID sectionId) {
        Section section = sections.stream()
            .filter(s -> s.getSectionId().equals(sectionId))
            .findFirst()
            .orElseThrow(() -> new SectionException.SectionNotFoundException(sectionId.toString()));
        
        this.sections.remove(section);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get read-only view of sections
     */
    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    private Section findSectionById(UUID sectionId) {
        return this.sections.stream()
            .filter(s -> s.getSectionId().equals(sectionId))
            .findFirst()
            .orElseThrow(() ->
                new SectionException.SectionNotFoundException(sectionId.toString())
            );
    }
    
    /** 
     * Add a new lesson to a section
     */
    public Lesson addLessonToSection(
        UUID sectionId,
        String title,
        int orderIndex
    ) {
        Section section = findSectionById(sectionId);
        return section.addLesson(title, orderIndex);
    }
    
    /**
     * Check if course is owned by instructor
     * 
     * @param instructorId instructor to check
     * @return true if owned by instructor
     */
    public boolean isOwnedBy(UUID instructorId) {
        return this.instructorId.equals(instructorId);
    }

    /**
     * Ensure course is owned by instructor, throw if not
     * 
     * @throws UnauthorizedAccessException if not owner
     */
    public void ensureOwnedBy(UUID instructorId) {
        if (!isOwnedBy(instructorId)) {
            throw new CourseException.UnauthorizedAccessException(
                "User " + instructorId + " is not owner of course " + this.courseId
            );
        }
    }

    /**
     * Check if course has minimum content to be published
     */
    public boolean hasMinimumContent() {
        return !this.sections.isEmpty() 
            && this.sections.stream().anyMatch(s -> !s.getLessons().isEmpty());
    }

    /**
     * Calculate total number of lessons in course
     */
    public int getTotalLessonsCount() {
        return this.sections.stream()
            .mapToInt(s -> s.getLessons().size())
            .sum();
    }

    /**
     * Check if course is ready to be published
     */
    public boolean isReadyForPublish() {
        return hasMinimumContent() 
            && this.price != null 
            && !this.price.isZero()
            && this.description != null 
            && this.description.length() >= 50;
    }

    /**
     * Check if course is free
     */
    public boolean isFree() {
        return this.price.isZero();
    }

    // ========== PRIVATE GUARD METHODS ==========

    /**
     * Guard against null values
     */
    private static void guardAgainstNull(Object value, String fieldName) {
        if (value == null) {
            throw new CourseException.CourseInvariantViolationException(fieldName + " cannot be null");
        }
    }

    /**
     * Guard against null or empty strings
     */
    private static void guardAgainstNullOrEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new CourseException.CourseInvariantViolationException(fieldName + " cannot be null or empty");
        }
    }

    /**
     * Guard against duplicate section titles
     */
    private void guardAgainstDuplicateSectionTitle(String title) {
        boolean exists = this.sections.stream()
            .anyMatch(s -> s.getTitle().equalsIgnoreCase(title));
        
        if (exists) {
            throw new CourseException.CourseInvariantViolationException(
                "Section with title '" + title + "' already exists in this course"
            );
        }
    }

    // ========== JPA LIFECYCLE CALLBACKS ==========

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // End of Course class
}
