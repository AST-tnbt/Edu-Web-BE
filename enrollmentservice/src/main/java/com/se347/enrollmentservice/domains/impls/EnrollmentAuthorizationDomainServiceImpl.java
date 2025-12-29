package com.se347.enrollmentservice.domains.impls;

import com.se347.enrollmentservice.clients.CourseServiceClient;
import com.se347.enrollmentservice.domains.EnrollmentAuthorizationDomainService;
import com.se347.enrollmentservice.entities.Enrollment;
import com.se347.enrollmentservice.exceptions.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of Cross-Aggregate Authorization Domain Service
 * 
 * DDD IMPLEMENTATION NOTES:
 * 
 * 1. WHY THIS IS A DOMAIN SERVICE:
 *    - Enforces business rules across aggregates
 *    - "Instructor ownership" is a domain concept
 *    - Reusable authorization logic
 * 
 * 2. WHY USE CLIENT:
 *    - Course aggregate lives in courseservice (separate bounded context)
 *    - Can't directly access Course repository
 *    - Client is the anti-corruption layer
 * 
 * 3. PERFORMANCE CONSIDERATION:
 *    - Caches could be added (future optimization)
 *    - Currently makes HTTP call per check
 *    - Trade-off: correctness > performance for authorization
 * 
 * 4. ERROR HANDLING:
 *    - If courseservice is down, fail-closed (deny access)
 *    - Security over availability
 */
@RequiredArgsConstructor
@Service
public class EnrollmentAuthorizationDomainServiceImpl implements EnrollmentAuthorizationDomainService {
    
    private final CourseServiceClient courseServiceClient;
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentAuthorizationDomainServiceImpl.class);
    
    /**
     * Ensure instructor owns the course
     * 
     * ALGORITHM:
     * 1. Query courseservice for actual instructor
     * 2. Compare with claimed instructor
     * 3. Throw ForbiddenException if mismatch
     * 
     * SECURITY:
     * - Fail-closed: if courseservice unavailable, deny access
     * - Log all authorization failures for audit
     */
    @Override
    public void ensureInstructorOwnsCourse(UUID courseId, UUID instructorId) {
        try {
            // Query actual instructor from courseservice
            UUID actualInstructorId = courseServiceClient.getInstructorIdByCourseId(courseId);
            
            // Compare
            if (!actualInstructorId.equals(instructorId)) {
                logger.warn(
                    "Authorization failed: Instructor {} attempted to access course {} owned by {}",
                    instructorId, courseId, actualInstructorId
                );
                throw new ForbiddenException(
                    "Instructor " + instructorId + " does not own course " + courseId
                );
            }
            
            logger.debug("Authorization success: Instructor {} owns course {}", instructorId, courseId);
            
        } catch (ForbiddenException e) {
            throw e; // Re-throw authorization failures
        } catch (Exception e) {
            // Fail-closed: if can't verify, deny access
            logger.error(
                "Authorization failed: Unable to verify instructor {} for course {} due to error",
                instructorId, courseId, e
            );
            throw new ForbiddenException(
                "Unable to verify course ownership. Access denied for security.",
                e
            );
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

