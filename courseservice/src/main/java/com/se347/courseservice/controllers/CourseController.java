package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.se347.courseservice.services.CourseService;
import com.se347.courseservice.dtos.CourseResponseDto;
import com.se347.courseservice.dtos.CourseRequestDto;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    CourseController(CourseService courseService){
        this.courseService = courseService;
    }
    
    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(
        @RequestBody CourseRequestDto request,
        @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request, userId));
    }

    @GetMapping("/id/{courseId}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable String courseId) {
        return ResponseEntity.ok(courseService.getCourseById(UUID.fromString(courseId)));
    }

    @GetMapping("/slug/{courseSlug}")
    public ResponseEntity<CourseResponseDto> getCourseByCourseSlug(
            @PathVariable String courseSlug) {
        return ResponseEntity.ok(courseService.getCourseByCourseSlug(courseSlug));
    }

    @PutMapping("/id/{courseId}")
    public ResponseEntity<CourseResponseDto> updateCourse(
            @PathVariable String courseId,
            @RequestBody CourseRequestDto request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(courseService.updateCourseById(UUID.fromString(courseId), request, userId));
    }

    @PutMapping("/slug/{courseSlug}")
    public ResponseEntity<CourseResponseDto> updateCourseByCourseSlug(
            @PathVariable String courseSlug, 
            @RequestBody CourseRequestDto request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(courseService.updateCourseByCourseSlug(courseSlug, request, userId));
    }

    @GetMapping
    public ResponseEntity<Page<CourseResponseDto>> getAllCourses(Pageable pageable) {
        return ResponseEntity.ok(courseService.getAllCourses(pageable));
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
