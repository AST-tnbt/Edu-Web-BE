package com.se347.courseservice.entities.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import jakarta.persistence.Column;

import com.se347.courseservice.utils.SlugUtil;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Slug {
    
    @Column(name = "slug", nullable = false, unique = true)
    private String value;
    
    public static Slug fromTitle(String title) {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        String slugValue = SlugUtil.toSlug(title);
        return new Slug(slugValue);
    }
    
    public static Slug of(String value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid slug format");
        }
        return new Slug(value);
    }
    
    private static boolean isValid(String slug) {
        return slug != null && slug.matches("^[a-z0-9-]+$");
    }
}
