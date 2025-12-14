package com.se347.courseservice.domains.impls;

import com.se347.courseservice.domains.SectionDomainService;
import com.se347.courseservice.entities.Section;
import com.se347.courseservice.dtos.SectionRequestDto;
import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.repositories.SectionRepository;
import com.se347.courseservice.domains.CourseDomainService;
import com.se347.courseservice.utils.SlugUtil;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionDomainServiceImpl implements SectionDomainService {
    
    private final SectionRepository sectionRepository;
    private final CourseDomainService courseDomainService;

    @Override
    @Transactional(readOnly = true)
    public Section findSectionById(UUID sectionId) {
        if (sectionId == null) {
            throw new CourseException.InvalidRequestException("Section ID cannot be null");
        }
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new CourseException.SectionNotFoundException(sectionId.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public Section findSectionBySlug(String sectionSlug) {
        if (sectionSlug == null || sectionSlug.isEmpty()) {
            throw new CourseException.InvalidRequestException("Section slug cannot be null or empty");
        }
        return sectionRepository.findBySectionSlug(sectionSlug)
                .orElseThrow(() -> new CourseException.SectionNotFoundException("Section with slug '" + sectionSlug + "' not found"));
    }       

    @Override
    @Transactional(readOnly = true)
    public List<Section> findSectionsByCourseSlug(String courseSlug) {
        if (courseSlug == null || courseSlug.isEmpty()) {
            throw new CourseException.InvalidRequestException("Course slug cannot be null or empty");
        }
        return sectionRepository.findByCourse_CourseSlug(courseSlug)
                .orElseThrow(() -> new CourseException.CourseNotFoundException("Course with slug '" + courseSlug + "' not found"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Section> findSectionsByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }
        return sectionRepository.findByCourse_CourseId(courseId)
                .orElseThrow(() -> new CourseException.CourseNotFoundException("Course with ID '" + courseId + "' not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Section toSection(UUID sectionId) {
        return findSectionById(sectionId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean sectionExists(UUID sectionId) {
        if (sectionId == null) {
            return false;
        }
        return sectionRepository.existsById(sectionId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean sectionExistsBySlug(String sectionSlug) {
        if (sectionSlug == null || sectionSlug.isEmpty()) {
            return false;
        }
        return sectionRepository.findBySectionSlug(sectionSlug).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean sectionExistsByTitle(String title) {
        if (title == null || title.isEmpty()) {
            return false;
        }
        return sectionRepository.existsByTitle(title);
    }

    @Override
    public Section createSectionEntity(SectionRequestDto request, UUID courseId) {
        Section section = Section.builder()
            .sectionSlug(generateSectionSlug(request.getTitle()))
            .title(request.getTitle())
            .description(request.getDescription())
            .orderIndex(request.getOrderIndex())
            .course(courseDomainService.findCourseById(courseId))
            .build();
        section.onCreate();
        return section;
    }

    @Override
    public Section updateSectionEntity(Section section, UUID courseId, SectionRequestDto request) {
        if (section == null) {
            throw new CourseException.SectionNotFoundException("Section cannot be null");
        }
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }

        section.setTitle(request.getTitle());
        section.setSectionSlug(generateSectionSlug(request.getTitle()));
        section.setDescription(request.getDescription());
        section.setOrderIndex(request.getOrderIndex());
        section.setCourse(courseDomainService.findCourseById(courseId));
        section.onUpdate();
        return section;
    }

    @Override
    public void validateSectionCreation(SectionRequestDto request, UUID courseId) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }
        if (courseId == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }
        if (!courseDomainService.courseExists(courseId)) {
            throw new CourseException.CourseNotFoundException("Course with ID '" + courseId + "' not found");
        }
    }

    @Override
    public void validateSectionUpdate(Section section, UUID courseId, SectionRequestDto request, UUID userId) {
        if (section == null) {
            throw new CourseException.SectionNotFoundException("Section cannot be null");
        }
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (courseId == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }
        if (!courseDomainService.courseExists(courseId)) {
            throw new CourseException.CourseNotFoundException("Course with ID '" + courseId + "' not found");
        }
        if (!section.getCourse().getCourseId().equals(courseId)) {
            throw new CourseException.SectionNotFoundException("Section does not belong to course");
        }
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            throw new CourseException.InvalidRequestException("Description cannot be null or empty");
        }
        if (request.getOrderIndex() <= 0) {
            throw new CourseException.InvalidRequestException("Order index must be greater than 0");
        }
        if (!isSectionOwner(section, userId)) {
            throw new CourseException.UnauthorizedAccessException("User not authorized to access this resource");
        }
    }

    @Override
    public void validateSectionBelongsToCourse(Section section, UUID courseId) {
        if (section == null) {
            throw new CourseException.SectionNotFoundException("Section cannot be null");
        }
        if (courseId == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }
        if (!section.getCourse().getCourseId().equals(courseId)) {
            throw new CourseException.SectionNotFoundException("Section does not belong to course");
        }
    }

    @Override
    public boolean isSectionOwner(Section section, UUID userId) {
        if (section == null || userId == null) {
            return false;
        }
        return section.getCourse().getInstructorId().equals(userId);
    }
    
    @Override
    public String generateSectionSlug(String title) {
        if (title == null || title.isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }
        return SlugUtil.toSlug(title);
    }
}