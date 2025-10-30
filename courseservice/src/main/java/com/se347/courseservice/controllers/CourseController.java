package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.se347.courseservice.services.CourseService;
import com.se347.courseservice.dtos.CourseResponseDto;
import com.se347.courseservice.dtos.CourseRequestDto;
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
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable String courseId) {
        return ResponseEntity.ok(courseService.getCourseById(UUID.fromString(courseId)));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponseDto> updateCourse(@PathVariable String courseId, @RequestBody CourseRequestDto request) {
        return ResponseEntity.ok(courseService.updateCourse(UUID.fromString(courseId), request));
    }

    @GetMapping
    public ResponseEntity<List<CourseResponseDto>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByCategoryName(@PathVariable String categoryName) {
        return ResponseEntity.ok(courseService.getCoursesByCategoryName(categoryName));
    }
}
