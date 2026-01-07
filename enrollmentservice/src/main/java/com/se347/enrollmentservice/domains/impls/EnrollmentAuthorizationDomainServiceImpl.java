package com.se347.enrollmentservice.domains.impls;

import com.se347.enrollmentservice.domains.EnrollmentAuthorizationDomainService;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.exceptions.ForbiddenException;
import com.se347.enrollmentservice.repositories.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class EnrollmentAuthorizationDomainServiceImpl implements EnrollmentAuthorizationDomainService {
    
    private final EnrollmentRepository enrollmentRepository;
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentAuthorizationDomainServiceImpl.class);
    
    @Override
    public void ensureInstructorOwnsCourse(UUID courseId, UUID instructorId) {
        try {
            UUID actualInstructorId = enrollmentRepository.findInstructorIdByCourseId(courseId);
            if (!actualInstructorId.equals(instructorId)) {
                throw new ForbiddenException("Instructor " + instructorId + " does not own course " + courseId);
            }
        } catch (Exception e) {
            throw new ForbiddenException("Unable to verify course ownership. Access denied for security.", e);
        }
    }
    
    /**
     * Ensure student owns the enrollment
     * 
     * BUSINESS RULE: Students can only view their own enrollments
     */
    @Override
    public void ensureStudentOwnsEnrollment(Enrollment enrollment, UUID studentId) {
        if (!enrollment.getStudentId().equals(studentId)) {
            logger.warn(
                "Authorization failed: Student {} attempted to access enrollment {} owned by {}",
                studentId, enrollment.getEnrollmentId(), enrollment.getStudentId()
            );
            throw new ForbiddenException(
                "Student " + studentId + " does not own enrollment " + enrollment.getEnrollmentId()
            );
        }
        
        logger.debug("Authorization success: Student {} owns enrollment {}", studentId, enrollment.getEnrollmentId());
    }
}

