package com.se347.courseservice.services.impl;

import com.se347.courseservice.services.SectionService;
import com.se347.courseservice.dtos.SectionRequestDto;
import com.se347.courseservice.dtos.SectionResponseDto;
import com.se347.courseservice.repositories.SectionRepository;
import com.se347.courseservice.services.CourseService;
import com.se347.courseservice.entities.Section;
import com.se347.courseservice.exceptions.CourseException;

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
    @Transactional(readOnly = true)
    public List<SectionResponseDto> getSectionsByCourseId(UUID courseId) {
        return sectionRepository.findByCourse_CourseId(courseId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private SectionResponseDto mapToResponse(Section section) {
        return SectionResponseDto.builder()
            .sectionId(section.getSectionId())
            .courseId(section.getCourse().getCourseId())
            .title(section.getTitle())
            .description(section.getDescription())
            .orderIndex(section.getOrderIndex())
            .createdAt(section.getCreatedAt())
            .updatedAt(section.getUpdatedAt())
            .build();
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
