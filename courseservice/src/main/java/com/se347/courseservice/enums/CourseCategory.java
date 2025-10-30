package com.se347.courseservice.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Extensible enum for course categories
 * Base categories are predefined, but new ones can be added dynamically
 */
public enum CourseCategory {
    // Predefined categories
    PROGRAMMING("Programming", "Software development and coding"),
    DESIGN("Design", "UI/UX and graphic design"),
    BUSINESS("Business", "Business and entrepreneurship"),
    MARKETING("Marketing", "Digital marketing and advertising"),
    DATA_SCIENCE("Data Science", "Data analysis and machine learning"),
    WEB_DEVELOPMENT("Web Development", "Frontend and backend development"),
    MOBILE_DEVELOPMENT("Mobile Development", "iOS and Android development"),
    CYBERSECURITY("Cybersecurity", "Information security and ethical hacking"),
    CLOUD_COMPUTING("Cloud Computing", "AWS, Azure, and cloud platforms"),
    DEVOPS("DevOps", "Development operations and deployment"),
    GAME_DEVELOPMENT("Game Development", "Video game design and development"),
    ARTIFICIAL_INTELLIGENCE("Artificial Intelligence", "AI and machine learning"),
    BLOCKCHAIN("Blockchain", "Cryptocurrency and blockchain technology"),
    DATABASE("Database", "Database design and management"),
    NETWORKING("Networking", "Computer networks and infrastructure");

    private final String displayName;
    private final String description;

    CourseCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get category by display name (case-insensitive)
     */
    public static CourseCategory fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }
        
        return Arrays.stream(values())
                .filter(category -> category.getDisplayName().equalsIgnoreCase(displayName.trim()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all display names
     */
    public static List<String> getAllDisplayNames() {
        return Arrays.stream(values())
                .map(CourseCategory::getDisplayName)
                .collect(Collectors.toList());
    }

    /**
     * Check if a display name exists
     */
    public static boolean isValidCategory(String displayName) {
        return fromDisplayName(displayName) != null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
