package com.se347.courseservice.domains;

import com.se347.courseservice.utils.SlugUtil;
import com.se347.courseservice.repositories.CourseRepository;
import com.se347.courseservice.repositories.SectionRepository;
import com.se347.courseservice.repositories.LessonRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class SlugGenerateService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;

    private final Random random = new Random();

    /**
     * Generate random alphabetic string
     * @param length length of random string
     * @return random string containing only letters (a-z, A-Z)
     */
    private String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public String generateCourseSlug(String input) {
        String baseSlug = SlugUtil.toSlug(input);
        if (baseSlug.isEmpty()) {
            baseSlug = "course";
        }
        
        String candidateSlug = baseSlug;
        int maxRetries = 100; // Prevent infinite loop
        int retryCount = 0;
        
        while (courseRepository.findByCourseSlug(candidateSlug).isPresent()) {
            if (retryCount >= maxRetries) {
                throw new IllegalStateException("Unable to generate unique course slug after " + maxRetries + " attempts");
            }
            candidateSlug = baseSlug + "-" + generateRandomString(4);
            retryCount++;
        }
        
        return candidateSlug;
    }

    public String generateSectionSlug(String input) {
        String baseSlug = SlugUtil.toSlug(input);
        if (baseSlug.isEmpty()) {
            baseSlug = "section";
        }
        
        String candidateSlug = baseSlug;
        int maxRetries = 100; // Prevent infinite loop
        int retryCount = 0;
        
        while (sectionRepository.findBySectionSlug(candidateSlug).isPresent()) {
            if (retryCount >= maxRetries) {
                throw new IllegalStateException("Unable to generate unique section slug after " + maxRetries + " attempts");
            }
            candidateSlug = baseSlug + "-" + generateRandomString(4);
            retryCount++;
        }
        
        return candidateSlug;
    }

    public String generateLessonSlug(String input) {
        String baseSlug = SlugUtil.toSlug(input);
        if (baseSlug.isEmpty()) {
            baseSlug = "lesson";
        }
        
        String candidateSlug = baseSlug;
        int maxRetries = 100; // Prevent infinite loop
        int retryCount = 0;
        
        while (lessonRepository.findByLessonSlug(candidateSlug).isPresent()) {
            if (retryCount >= maxRetries) {
                throw new IllegalStateException("Unable to generate unique lesson slug after " + maxRetries + " attempts");
            }
            candidateSlug = baseSlug + "-" + generateRandomString(4);
            retryCount++;
        }
        
        return candidateSlug;
    }
}