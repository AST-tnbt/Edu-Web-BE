package com.se347.contentservice.controllers;

import com.se347.contentservice.entities.ContentFile;
import com.se347.contentservice.services.ContentFileService;
import com.se347.contentservice.services.ContentFileServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/content-files")
public class ContentFileController {

    private final ContentFileService service;

    public ContentFileController(ContentFileService service) {
        this.service = service;
    }

    // =====================================
    // Upload File
    // =====================================
    @PostMapping("/upload/{contentId}")
    public ResponseEntity<ContentFile> uploadFile(
            @PathVariable UUID contentId,
            @RequestParam("file") MultipartFile file
    ) {
        ContentFile uploaded = service.uploadFile(file, contentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }

    // =====================================
    // Get Single File
    // =====================================
    @GetMapping("/{id}")
    public ResponseEntity<ContentFile> getFile(@PathVariable UUID id) {
        ContentFile file = service.getFile(id);
        return ResponseEntity.ok(file);
    }

    // =====================================
    // List Files by Content ID
    // =====================================
    @GetMapping("/list/{contentId}")
    public ResponseEntity<List<ContentFile>> listFiles(@PathVariable UUID contentId) {
        List<ContentFile> files = service.listFiles(contentId);
        return ResponseEntity.ok(files);
    }

    // =====================================
    // Delete File
    // =====================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        service.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}


