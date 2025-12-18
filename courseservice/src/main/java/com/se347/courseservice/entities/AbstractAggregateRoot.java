package com.se347.courseservice.entities;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all Aggregate Roots.
 * Provides domain event support through Spring Data JPA.
 * 
 * Usage:
 * 1. Aggregate root extends this class
 * 2. Call registerEvent() to queue events
 * 3. When repository.save() is called, Spring auto-publishes events
 * 4. Events are cleared after publication
 */
@MappedSuperclass
public abstract class AbstractAggregateRoot<T extends AbstractAggregateRoot<T>> {
    
    @Transient
    private final List<Object> domainEvents = new ArrayList<>();
    
    /**
     * Register a domain event to be published after save
     * 
     * Example:
     *   registerEvent(new CourseCreatedEvent(this.courseId));
     */
    protected void registerEvent(Object event) {
        this.domainEvents.add(event);
    }
    
    /**
     * Called by Spring Data JPA after save() to get events
     * Do NOT call manually
     */
    @DomainEvents
    public Collection<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    /**
     * Called by Spring Data JPA after events are published
     * Do NOT call manually
     */
    @AfterDomainEventPublication
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    /**
     * Check if there are pending events
     */
    protected boolean hasPendingEvents() {
        return !domainEvents.isEmpty();
    }
}