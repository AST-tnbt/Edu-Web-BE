package com.se347.enrollmentservice.domains;

import java.util.UUID;
import java.util.List;
import com.se347.enrollmentservice.dtos.MyCourseProgressDto;
import com.se347.enrollmentservice.dtos.EnrollmentRequestDto;
import com.se347.enrollmentservice.dtos.EnrollmentResponseDto;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.enums.EnrollmentStatus;
import com.se347.enrollmentservice.enums.PaymentStatus;

public interface EnrollmentDomainService {
    // Orchestration methods (gọi EnrollmentService + các service khác)
    EnrollmentResponseDto createEnrollment(EnrollmentRequestDto request);
    List<MyCourseProgressDto> getMyCourseProgress(UUID studentId);
    void ensureEnrollmentAccessible(UUID enrollmentId);
    void syncCourseProgressOnLessonChange(UUID enrollmentId, boolean increment);
    
    // Enrollment Entity CRUD operations (trả về entity)
    Enrollment findEnrollmentById(UUID enrollmentId);
    Enrollment toEnrollment(UUID enrollmentId);
    boolean enrollmentExists(UUID enrollmentId);
    List<Enrollment> findEnrollmentsByStudentId(UUID studentId);
    List<Enrollment> findEnrollmentsByCourseId(UUID courseId);
    List<Enrollment> findEnrollmentsByCourseIdAndStudentId(UUID courseId, UUID studentId);
    List<Enrollment> findAllEnrollments();

    // Authorization checks
    boolean canUserAccessEnrollment(Enrollment enrollment, UUID userId);
    boolean canInstructorAccessEnrollment(Enrollment enrollment, UUID instructorId);
    
    // Entity operations
    Enrollment createEnrollmentEntity(EnrollmentRequestDto request);
    Enrollment updateEnrollmentEntity(Enrollment enrollment, EnrollmentRequestDto request);
    Enrollment patchEnrollmentEntity(Enrollment enrollment, EnrollmentRequestDto request);
    Enrollment updateEnrollmentStatusEntity(Enrollment enrollment, EnrollmentStatus newStatus);
    Enrollment updatePaymentStatusEntity(Enrollment enrollment, PaymentStatus newStatus);
    Enrollment updateStatusesEntity(Enrollment enrollment, EnrollmentStatus enrollmentStatus, PaymentStatus paymentStatus);

    // Business validations
    void validateEnrollmentCreation(EnrollmentRequestDto request);
    void validateEnrollmentUpdate(Enrollment enrollment, EnrollmentRequestDto request);
    void validateNoDuplicateEnrollment(UUID currentEnrollmentId, UUID courseId, UUID studentId);
    void validateStatusTransition(EnrollmentStatus currentStatus, EnrollmentStatus newStatus);
    void validatePaymentStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus);
}
