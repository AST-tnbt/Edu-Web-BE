package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.se347.courseservice.services.SectionService;
import com.se347.courseservice.dtos.SectionRequestDto;
import com.se347.courseservice.dtos.SectionResponseDto;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @PostMapping("/sections")
    public ResponseEntity<SectionResponseDto> createSection(@RequestBody SectionRequestDto request) {
        return ResponseEntity.ok(sectionService.createSection(request));
    }

    @GetMapping("/sections/{sectionId}")
    public ResponseEntity<SectionResponseDto> getSectionById(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(sectionService.getSectionById(sectionId));
    }

    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<SectionResponseDto> updateSection(@PathVariable UUID sectionId, @RequestBody SectionRequestDto request) {
        return ResponseEntity.ok(sectionService.updateSection(sectionId, request));
    }

    @GetMapping("/courses/{courseId}/sections")
    public ResponseEntity<List<SectionResponseDto>> getSectionsByCourseId(@PathVariable UUID courseId) {
        return ResponseEntity.ok(sectionService.getSectionsByCourseId(courseId));
    }
}
