package com.se347.courseservice.entities;

import com.se347.courseservice.enums.ContentType;
import com.se347.courseservice.enums.ContentStatus;
import com.se347.courseservice.entities.valueobjects.OrderIndex;
import com.se347.courseservice.exceptions.CourseException.*;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content {
    
    @Id
    private UUID contentId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContentType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = true)
    private String contentUrl;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String textContent;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "order_index", nullable = false))
    })
    private OrderIndex orderIndex;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ========== FACTORY METHOD (PACKAGE-PRIVATE) ==========
    
    /**
     * Create new content (can only be called from Lesson entity)
     * Package-private visibility enforces aggregate boundary
     */
    static Content createNew(
        ContentType type,
        String title,
        String contentUrl,
        String textContent,
        int orderIndex,
        Lesson lesson
    ) {
        guardAgainstNull(type, "Content type");
        guardAgainstNullOrEmpty(title, "Content title");
        guardAgainstNull(lesson, "Lesson");
        validateContentData(type, contentUrl, textContent);
        
        Content content = new Content();
        content.contentId = UUID.randomUUID();
        content.type = type;
        content.title = title;
        content.contentUrl = contentUrl;
        content.textContent = textContent;
        content.orderIndex = OrderIndex.of(orderIndex);
        content.status = ContentStatus.DRAFT; // Default status
        content.lesson = lesson;
        content.createdAt = LocalDateTime.now();
        content.updatedAt = LocalDateTime.now();
        
        return content;
    }
    
    // ========== DOMAIN METHODS ==========
    
    /**
     * Update content details
     */
    public void updateDetails(
        String title,
        String contentUrl,
        String textContent,
        int orderIndex
    ) {
        guardAgainstNullOrEmpty(title, "Title");
        validateContentData(this.type, contentUrl, textContent);
        
        this.title = title;
        this.contentUrl = contentUrl;
        this.textContent = textContent;
        this.orderIndex = OrderIndex.of(orderIndex);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Publish content (change status to PUBLISHED)
     */
    public void publish() {
        if (this.status == ContentStatus.PUBLISHED) {
            throw new InvalidContentStateException("Content is already published");
        }
        
        validateContentReadyForPublish();
        
        this.status = ContentStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Unpublish content (change status back to DRAFT)
     */
    public void unpublish() {
        if (this.status != ContentStatus.PUBLISHED) {
            throw new InvalidContentStateException("Content is not published");
        }
        
        this.status = ContentStatus.DRAFT;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Archive content
     */
    public void archive() {
        this.status = ContentStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if content is published
     */
    public boolean isPublished() {
        return this.status == ContentStatus.PUBLISHED;
    }
    
    /**
     * Check if content is draft
     */
    public boolean isDraft() {
        return this.status == ContentStatus.DRAFT;
    }
    
    /**
     * Check if content is video
     */
    public boolean isVideo() {
        return this.type == ContentType.VIDEO;
    }
    
    /**
     * Check if content is text
     */
    public boolean isText() {
        return this.type == ContentType.TEXT;
    }
    
    /**
     * Check if content belongs to specific lesson
     */
    public boolean belongsToLesson(UUID lessonId) {
        return this.lesson.getLessonId().equals(lessonId);
    }
    
    // ========== GUARDS ==========
    
    private static void guardAgainstNull(Object value, String fieldName) {
        if (value == null) {
            throw new InvalidContentStateException(fieldName + " cannot be null");
        }
    }
    
    private static void guardAgainstNullOrEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidContentStateException(fieldName + " cannot be null or empty");
        }
    }
    
    /**
     * Validate content data based on type
     */
    private static void validateContentData(ContentType type, String contentUrl, String textContent) {
        switch (type) {
            case VIDEO:
            case DOCUMENT:
                if (contentUrl == null || contentUrl.trim().isEmpty()) {
                    throw new InvalidContentStateException(
                        type + " content must have a valid contentUrl"
                    );
                }
                break;
            case TEXT:
                if (textContent == null || textContent.trim().isEmpty()) {
                    throw new InvalidContentStateException(
                        "TEXT content must have textContent"
                    );
                }
                break;
            default:
                // Other types can have either URL or text
                break;
        }
    }
    
    /**
     * Validate content is ready to be published
     */
    private void validateContentReadyForPublish() {
        if (this.title == null || this.title.trim().isEmpty()) {
            throw new InvalidContentStateException("Cannot publish content without title");
        }
        
        validateContentData(this.type, this.contentUrl, this.textContent);
    }
    
    // ========== JPA CALLBACKS ==========
    
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
}
