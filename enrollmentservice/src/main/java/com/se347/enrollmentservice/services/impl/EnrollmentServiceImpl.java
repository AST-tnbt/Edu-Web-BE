package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;

import com.se347.enrollmentservice.services.EnrollmentService;
import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.repositories.EnrollmentRepository;
import com.se347.enrollmentservice.enums.EnrollmentStatus;
import com.se347.enrollmentservice.enums.PaymentStatus;
import com.se347.enrollmentservice.exceptions.EnrollmentException;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }
    
    @Override
    public EnrollmentResponseDto createEnrollment(EnrollmentRequestDto request) {

        // if (request == null) {
        //     throw new EnrollmentException.InvalidRequestException("Request cannot be null");
        // }

        // if (request.getCourseId() == null) {
        //     throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        // }

        // if (request.getStudentId() == null) {
        //     throw new EnrollmentException.InvalidRequestException("Student ID cannot be null");
        // }

        Enrollment enrollment = Enrollment.builder()
            .courseId(request.getCourseId())
            .studentId(request.getStudentId())
            .enrolledAt(request.getEnrolledAt())
            .enrollmentStatus(request.getEnrollmentStatus())
            .paymentStatus(request.getPaymentStatus())
            .accessExpiresAt(request.getAccessExpiresAt())
            .build();

        enrollment.onCreate();
        enrollment.onUpdate();
        enrollmentRepository.save(enrollment);
        return mapToResponse(enrollment);
    }

    @Override
    public EnrollmentResponseDto getEnrollmentById(UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException(enrollmentId.toString()));
        return mapToResponse(enrollment);
    }


    @Override
    public List<EnrollmentResponseDto> getEnrollmentsByStudentId(UUID studentId) {
        return enrollmentRepository.findByStudentId(studentId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentResponseDto> getEnrollmentsByCourseId(UUID courseId) {
        return enrollmentRepository.findByCourseId(courseId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public EnrollmentResponseDto updateEnrollment(UUID enrollmentId, EnrollmentRequestDto request) {

        if (request == null) {
            throw new EnrollmentException.InvalidRequestException("Request cannot be null");
        }

        if (request.getCourseId() == null) {
            throw new EnrollmentException.InvalidRequestException("Course ID cannot be null");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException(enrollmentId.toString()));

        enrollment.setCourseId(request.getCourseId());
        enrollment.setStudentId(request.getStudentId());
        enrollment.setEnrolledAt(request.getEnrolledAt());
        enrollment.setEnrollmentStatus(request.getEnrollmentStatus());
        enrollment.setPaymentStatus(request.getPaymentStatus());
        enrollment.setAccessExpiresAt(request.getAccessExpiresAt());
        enrollment.onUpdate();

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public boolean isEnrollmentExists(UUID enrollmentId) {
        return enrollmentRepository.existsById(enrollmentId);
    }

    @Override
    public List<EnrollmentResponseDto> getEnrollmentsByCourseIdAndStudentId(UUID courseId, UUID studentId) {
        return enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId) 
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentResponseDto> getAllEnrollments() {
        return enrollmentRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // @Override
    // public EnrollmentResponseDto updateStatusOrPaymentStatusOfEnrollment(UUID enrollmentId,EnrollmentRequestDto request) {
    //     Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
    //         .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException(enrollmentId.toString()));
        
    //     request.forEach((key, value) -> {
    //         if (key.equals("enrollmentStatus")) {
    //             enrollment.setEnrollmentStatus((EnrollmentStatus) value);
    //         } else if (key.equals("paymentStatus")) {
    //             enrollment.setPaymentStatus((PaymentStatus) value);
    //         }
    //     });
        
    //     return mapToResponse(enrollmentRepository.save(enrollment));
    // }

    // @Override
    // public EnrollmentResponseDto updatePaymentStatusOfEnrollment(UUID enrollmentId, PaymentStatus status) {
    //     Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
    //         .orElseThrow(() -> new EnrollmentException.EnrollmentNotFoundException(enrollmentId.toString()));
    //     enrollment.setPaymentStatus(status);
    //     enrollment.onUpdate();
    //     return mapToResponse(enrollmentRepository.save(enrollment));
    // }

    private EnrollmentResponseDto mapToResponse(Enrollment enrollment) {
        return EnrollmentResponseDto.builder()
            .enrollmentId(enrollment.getEnrollmentId())
            .courseId(enrollment.getCourseId())
            .studentId(enrollment.getStudentId())
            .enrolledAt(enrollment.getEnrolledAt())
            .enrollmentStatus(enrollment.getEnrollmentStatus())
            .paymentStatus(enrollment.getPaymentStatus())
            .accessExpiresAt(enrollment.getAccessExpiresAt())
            .createdAt(enrollment.getCreatedAt())
            .updatedAt(enrollment.getUpdatedAt())
            .build();
    }
}
