package com.se347.enrollmentservice.entities;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base class for Aggregate Roots
 * 
 * DDD PATTERN: Aggregate Root
 * 
 * RESPONSIBILITIES:
 * - Manage domain events
 * - Provide event registration mechanism
 * - Auto-publish events after save (Spring Data JPA)
 * 
 * USAGE:
 * ```java
 * public class Enrollment extends AbstractAggregateRoot<Enrollment> {
 *     public Enrollment enroll(...) {
 *         // ... domain logic ...
 *         registerEvent(new EnrollmentCreatedEvent(...));
 *         return this;
 *     }
 * }
 * ```
 */
@MappedSuperclass
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractAggregateRoot {
    
    @Transient
    private final List<Object> domainEvents = new ArrayList<>();
    
    /**
     * Register a domain event to be published after save
     * 
     * @param event The domain event to register
     */
    protected void registerEvent(Object event) {
        this.domainEvents.add(event);
    }
    
    /**
     * Get all registered domain events (called by Spring Data JPA)
     * 
     * @return Collection of domain events
     */
    @DomainEvents
    protected Collection<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    /**
     * Clear domain events after publication (called by Spring Data JPA)
     */
    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

