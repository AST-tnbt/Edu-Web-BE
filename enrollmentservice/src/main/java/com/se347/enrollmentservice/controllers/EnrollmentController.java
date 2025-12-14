package com.se347.enrollmentservice.controllers;

import org.springframework.web.bind.annotation.*;

import com.se347.enrollmentservice.services.EnrollmentService;
import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.exceptions.EnrollmentException;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor        
@RestController
@RequestMapping("/api")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/admin/enrollments")
    public ResponseEntity<EnrollmentResponseDto> createEnrollmentInternal(
        @RequestBody EnrollmentRequestDto request,
        @RequestHeader("X-User-Id") UUID adminUserId,
        @RequestHeader("X-User-Roles") String userRoles
    ) {
        // Validate admin role
        if (userRoles == null || !userRoles.equals("ADMIN")) {
            throw new EnrollmentException.UnauthorizedAccessException("User does not have ADMIN role");
        }
        return ResponseEntity.ok(enrollmentService.createEnrollment(request));
    }

    @GetMapping("/enrollments/id/{enrollmentId}")
    public ResponseEntity<EnrollmentResponseDto> getEnrollmentById(
            @PathVariable UUID enrollmentId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(enrollmentId, userId));
    }

    @GetMapping("/enrollments/student/id/{studentId}")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByStudentId(
        @PathVariable UUID studentId,
        @RequestHeader("X-User-Id") UUID userId
    ) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudentId(studentId, userId));
    }

    @GetMapping("/courses/id/{courseId}/enrollments")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByCourseId(@PathVariable UUID courseId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourseId(courseId));
    }

    @GetMapping("/courses/id/{courseId}/enrollments/student/id/{studentId}")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByCourseIdAndStudentId(
            @PathVariable UUID courseId, 
            @PathVariable UUID studentId
        ) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourseIdAndStudentId(courseId, studentId));
    }

    @GetMapping("/enrollments/my-courses")
    public ResponseEntity<List<EnrollmentResponseDto>> getMyCourses(
        @RequestHeader("X-User-Id") UUID userId
    ) {
        return ResponseEntity.ok(enrollmentService.getMyCourses(userId));
    }

    @PutMapping("/enrollments/id/{enrollmentId}")
    public ResponseEntity<EnrollmentResponseDto> updateEnrollment(
        @PathVariable UUID enrollmentId, 
        @RequestBody EnrollmentRequestDto request,
        @RequestHeader("X-User-Id") UUID userId
    ) {
        return ResponseEntity.ok(enrollmentService.updateEnrollment(enrollmentId, request, userId));
    }
}