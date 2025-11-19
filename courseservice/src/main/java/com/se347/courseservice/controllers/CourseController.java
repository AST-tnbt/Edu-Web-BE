package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.se347.courseservice.services.CourseService;
import com.se347.courseservice.dtos.CourseResponseDto;
import com.se347.courseservice.dtos.CourseRequestDto;
import com.se347.courseservice.exceptions.CourseException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    CourseController(CourseService courseService){
        this.courseService = courseService;
    }
    
    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(@RequestBody CourseRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable String courseId, @RequestHeader("X-User-Roles") String userRoles) {
        if (!userRoles.contains("ADMIN")) {
            throw new CourseException.UnauthorizedAccessException("User not authorized to access this resource");
        }
        return ResponseEntity.ok(courseService.getCourseById(UUID.fromString(courseId)));
    }

    @GetMapping("/{courseSlug}")
    public ResponseEntity<CourseResponseDto> getCourseByCourseSlug(@PathVariable String courseSlug, @RequestHeader("X-User-Roles") String userRoles) {
        return ResponseEntity.ok(courseService.getCourseByCourseSlug(courseSlug));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponseDto> updateCourse(@PathVariable String courseId, @RequestBody CourseRequestDto request, @RequestHeader("X-User-Roles") String userRoles) {
        if (!userRoles.contains("ADMIN")) {
            throw new CourseException.UnauthorizedAccessException("User not authorized to access this resource");
        }
        return ResponseEntity.ok(courseService.updateCourse(UUID.fromString(courseId), request));
    }

    @PutMapping("/{courseSlug}")
    public ResponseEntity<CourseResponseDto> updateCourseByCourseSlug(@PathVariable String courseSlug, @RequestBody CourseRequestDto request, @RequestHeader("X-User-Roles") String userRoles, @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(courseService.updateCourseByCourseSlug(courseSlug, request, userRoles, UUID.fromString(userId)));
    }

    @GetMapping
    public ResponseEntity<List<CourseResponseDto>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByCategoryName(@PathVariable String categoryName) {
        return ResponseEntity.ok(courseService.getCoursesByCategoryName(categoryName));
    }

    @GetMapping("/{courseId}/total-lessons")
    public ResponseEntity<Integer> getTotalLessonsByCourseId(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseService.getToltalLessonsByCourseId(courseId));
    }
}
