package com.se347.enrollmentservice.domains;

import com.se347.enrollmentservice.entities.Enrollment;
import java.util.UUID;

public interface EnrollmentAuthorizationDomainService {
    void ensureInstructorOwnsCourse(UUID courseId, UUID instructorId);
    void ensureStudentOwnsEnrollment(Enrollment enrollment, UUID studentId);
}

