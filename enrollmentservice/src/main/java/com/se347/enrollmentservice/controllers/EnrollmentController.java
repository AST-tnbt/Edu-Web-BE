package com.se347.enrollmentservice.controllers;

import org.springframework.web.bind.annotation.*;

import com.se347.enrollmentservice.services.EnrollmentCommandService;
import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.exceptions.EnrollmentException;
import com.se347.enrollmentservice.services.EnrollmentQueryService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor        
@RestController
@RequestMapping("/api")
public class EnrollmentController {

    private final EnrollmentCommandService enrollmentCommandService;
    private final EnrollmentQueryService enrollmentQueryService;

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
        return ResponseEntity.ok(enrollmentCommandService.createEnrollment(request));
    }

    @GetMapping("/enrollments/id/{enrollmentId}")
    public ResponseEntity<EnrollmentResponseDto> getEnrollmentById(
            @PathVariable UUID enrollmentId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(enrollmentQueryService.getEnrollmentById(enrollmentId, userId));
    }

    @GetMapping("/courses/id/{courseId}/enrollments")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByCourseId(@PathVariable UUID courseId) {
        return ResponseEntity.ok(enrollmentQueryService.getEnrollmentsByCourseId(courseId));
    }

    @GetMapping("/courses/id/{courseId}/enrollments/student/id/{studentId}")
    public ResponseEntity<EnrollmentResponseDto> getEnrollmentByCourseIdAndStudentId(
            @PathVariable UUID courseId, 
            @PathVariable UUID studentId
        ) {
        return ResponseEntity.ok(enrollmentQueryService.getEnrollmentByCourseIdAndStudentId(courseId, studentId));
    }

    @GetMapping("/enrollments/my-courses")
    public ResponseEntity<List<EnrollmentResponseDto>> getMyCourses(
        @RequestHeader("X-User-Id") UUID userId
    ) {
        return ResponseEntity.ok(enrollmentQueryService.getEnrollmentsByStudentId(userId));
    }

    @PutMapping("/enrollments/id/{enrollmentId}")
    public ResponseEntity<EnrollmentResponseDto> updateEnrollment(
        @PathVariable UUID enrollmentId, 
        @RequestBody EnrollmentRequestDto request,
        @RequestHeader("X-User-Id") UUID userId
    ) {
        return ResponseEntity.ok(enrollmentCommandService.updateEnrollmentStatus(enrollmentId, request.getEnrollmentStatus(), userId));
    }
}