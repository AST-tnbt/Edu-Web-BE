package com.se347.enrollmentservice.controllers;

import org.springframework.web.bind.annotation.*;

import com.se347.enrollmentservice.services.EnrollmentService;
import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    EnrollmentController(EnrollmentService enrollmentService){
        this.enrollmentService = enrollmentService;
    }
    
    @PostMapping("/courses/{courseId}/enroll")
    public ResponseEntity<EnrollmentResponseDto> createEnrollment(@RequestBody EnrollmentRequestDto request) {
        return ResponseEntity.ok(enrollmentService.createEnrollment(request));
    }

    @GetMapping("enrollments/{enrollmentId}")
    public ResponseEntity<EnrollmentResponseDto> getEnrollmentById(@PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(enrollmentId));
    }

    @GetMapping("enrollments/my-courses")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByCurrentStudent(@RequestHeader("X-User-Id") UUID studentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudentId(studentId));
    }

    @GetMapping("courses/{courseId}/enrollments")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByCourseId(@PathVariable UUID courseId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourseId(courseId));
    }

    @GetMapping("courses/{courseId}/enrollments/{studentId}")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByCourseIdAndStudentId(@PathVariable UUID courseId, @PathVariable UUID studentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourseIdAndStudentId(courseId, studentId));
    }

    @GetMapping("enrollments/{studentId}")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByStudentId(@PathVariable UUID studentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudentId(studentId));
    }

    // @PatchMapping("enrollments/{enrollmentId}/status")
    // public ResponseEntity<EnrollmentResponseDto> updateEnrollmentStatus(@PathVariable UUID enrollmentId, @RequestBody EnrollmentRequestDto request) {
    //     return ResponseEntity.ok(enrollmentService.updateEnrollmentStatus(enrollmentId, request));
    // }

    // @PatchMapping("enrollments/{enrollmentId}/progress")
    // public ResponseEntity<EnrollmentResponseDto> updateEnrollmentProgress(@PathVariable UUID enrollmentId, @RequestBody EnrollmentRequestDto request) {
    //     return ResponseEntity.ok(enrollmentService.updateEnrollmentProgress(enrollmentId, request));
    // }

    @PutMapping("enrollments/{enrollmentId}")
    public ResponseEntity<EnrollmentResponseDto> updateEnrollment(@PathVariable UUID enrollmentId, @RequestBody EnrollmentRequestDto request) {
        return ResponseEntity.ok(enrollmentService.updateEnrollment(enrollmentId, request));
    }
}