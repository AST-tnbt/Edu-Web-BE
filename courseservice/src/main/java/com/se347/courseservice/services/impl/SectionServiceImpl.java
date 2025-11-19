package com.se347.courseservice.services.impl;

import com.se347.courseservice.services.SectionService;
import com.se347.courseservice.dtos.SectionRequestDto;
import com.se347.courseservice.dtos.SectionResponseDto;
import com.se347.courseservice.repositories.SectionRepository;
import com.se347.courseservice.services.CourseService;
import com.se347.courseservice.entities.Section;
import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.utils.SlugUtil;
import com.se347.courseservice.entities.Course;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SectionServiceImpl implements SectionService {
    private final SectionRepository sectionRepository;
    private final CourseService courseService;

    @Override
    @Transactional
    public SectionResponseDto createSection(SectionRequestDto request) {
        validateCreateRequest(request);

        String normalizedTitle = request.getTitle().trim();
        String normalizedDescription = request.getDescription().trim();

        if (sectionRepository.existsByCourse_CourseIdAndTitle(request.getCourseId(), normalizedTitle)) {
            throw new CourseException.InvalidRequestException("Section with title '" + normalizedTitle + "' already exists for course '" + request.getCourseId() + "'");
        }

        Section section = Section.builder()
            .sectionSlug(request.getSectionSlug() != null ? request.getSectionSlug() : SlugUtil.toSlug(request.getTitle()))
            .title(normalizedTitle)
            .description(normalizedDescription)
            .orderIndex(request.getOrderIndex())
            .course(courseService.toCourse(request.getCourseId()))
            .build();
        section.onCreate();
        sectionRepository.save(section);
        return mapToResponse(section);
    }

    @Override
    @Transactional(readOnly = true)
    public SectionResponseDto getSectionById(UUID sectionId) {
        if (sectionId == null) {
            throw new CourseException.InvalidRequestException("Section ID cannot be null");
        }

        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new CourseException.SectionNotFoundException(sectionId.toString()));
        return mapToResponse(section);
    }

    @Override
    @Transactional(readOnly = true)
    public Section toSection(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new CourseException.SectionNotFoundException(sectionId.toString()));
        return section;
    }

    @Override
    @Transactional
    public SectionResponseDto updateSection(UUID sectionId, SectionRequestDto request) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new CourseException.SectionNotFoundException(sectionId.toString()));

        validateUpdateRequest(request);

        String normalizedTitle = request.getTitle().trim();
        String normalizedDescription = request.getDescription().trim();

        // Optional duplicate check when title changes
        if (!normalizedTitle.equals(section.getTitle()) && sectionRepository.existsByCourse_CourseIdAndTitle(section.getCourse().getCourseId(), normalizedTitle)) {
            throw new CourseException.InvalidRequestException("Section with title '" + normalizedTitle + "' already exists for course '" + section.getCourse().getCourseId() + "'");
        }

        section.setSectionSlug(request.getSectionSlug() != null ? request.getSectionSlug() : SlugUtil.toSlug(request.getTitle()));
        section.setTitle(normalizedTitle);
        section.setDescription(normalizedDescription);
        section.setOrderIndex(request.getOrderIndex());
        section.onUpdate();
        sectionRepository.save(section);
        return mapToResponse(section);
    }

    @Override
    public boolean sectionExists(UUID sectionId) {
        return sectionRepository.existsById(sectionId);
    }

    @Override
    public List<SectionResponseDto> getSectionsByCourseSlug(String courseSlug) {
        if (courseSlug == null || courseSlug.trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Course slug cannot be null or empty");
        }
        List<Section> sections = sectionRepository.findByCourse_CourseSlug(courseSlug);
        if (sections.isEmpty()) {
            throw new CourseException.SectionNotFoundException("Sections with course slug '" + courseSlug + "' not found");
        }
        return sections.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionResponseDto> getSectionsByCourseId(UUID courseId) {
        return sectionRepository.findByCourse_CourseId(courseId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public SectionResponseDto getSectionByCourseSlugAndSectionSlug(String courseSlug, String sectionSlug) {
        if (courseSlug == null || courseSlug.trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Course slug cannot be null or empty");
        }
        if (sectionSlug == null || sectionSlug.trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Section slug cannot be null or empty");
        }

        Course course = courseService.toCourse(courseService.getCourseByCourseSlug(courseSlug).getCourseId());
        Section section = sectionRepository.findBySectionSlug(sectionSlug)
            .orElseThrow(() -> new CourseException.SectionNotFoundException("Section with slug '" + sectionSlug + "' not found"));
        if (section.getCourse().getCourseSlug() != course.getCourseSlug()) {
            throw new CourseException.SectionNotFoundException("Section with slug '" + sectionSlug + "' not found in course '" + courseSlug + "'");
        }
        return mapToResponse(section);
    }

    @Override
    @Transactional
    public SectionResponseDto updateSectionByCourseSlugAndSectionSlug(String courseSlug, String sectionSlug, SectionRequestDto request, String userRoles, UUID userId) {
        if (courseSlug == null || courseSlug.trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Course slug cannot be null or empty");
        }
        if (sectionSlug == null || sectionSlug.trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Section slug cannot be null or empty");
        }
    
        Course course = courseService.toCourse(courseService.getCourseByCourseSlug(courseSlug).getCourseId());
        Section section = sectionRepository.findBySectionSlug(sectionSlug)
            .orElseThrow(() -> new CourseException.SectionNotFoundException("Section with slug '" + sectionSlug + "' not found"));

        if (!authorizeAccess(section.getSectionId(), userRoles, userId)) {
            throw new CourseException.UnauthorizedAccessException("User not authorized to access this resource");
        }

        if (section.getCourse().getCourseSlug() != course.getCourseSlug()) {
            throw new CourseException.SectionNotFoundException("Section with slug '" + sectionSlug + "' not found in course '" + courseSlug + "'");
        }
        return updateSection(section.getSectionId(), request);
    }

    private SectionResponseDto mapToResponse(Section section) {
        return SectionResponseDto.builder()
            .sectionId(section.getSectionId())
            .sectionSlug(section.getSectionSlug() != null ? section.getSectionSlug() : SlugUtil.toSlug(section.getTitle()))
            .courseId(section.getCourse().getCourseId())
            .title(section.getTitle())
            .description(section.getDescription())
            .orderIndex(section.getOrderIndex())
            .createdAt(section.getCreatedAt())
            .updatedAt(section.getUpdatedAt())
            .build();
    }   

    private boolean authorizeAccess(UUID sectionId, String userRoles, UUID userId) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new CourseException.SectionNotFoundException(sectionId.toString()));
        if (!userRoles.contains("ADMIN") && section.getCourse().getInstructorId() != userId) {
            return false;
        }
        return true;
    }

    private void validateCreateRequest(SectionRequestDto request) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (request.getCourseId() == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }
        if (!courseService.courseExists(request.getCourseId())) {
            throw new CourseException.CourseNotFoundException(request.getCourseId().toString());
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Description cannot be null or empty");
        }
        if (request.getOrderIndex() <= 0) {
            throw new CourseException.InvalidRequestException("Order index must be greater than 0");
        }
    }

    private void validateUpdateRequest(SectionRequestDto request) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Description cannot be null or empty");
        }
        if (request.getOrderIndex() <= 0) {
            throw new CourseException.InvalidRequestException("Order index must be greater than 0");
        }
    }
}
