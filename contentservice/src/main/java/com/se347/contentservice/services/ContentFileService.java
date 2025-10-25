package com.se347.contentservice.services;

import com.se347.contentservice.dtos.ContentFileUpdateRequest;
import com.se347.contentservice.entities.ContentFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ContentFileService {

    /**
     * Upload a file to the storage and save metadata in the database.
     *
     * @param file the file to upload
     * @param contentId the ID of the related content
     * @return the saved ContentFile entity
     */
    ContentFile uploadFile(MultipartFile file, UUID contentId);

    /**
     * List all files for a given content ID.
     *
     * @param contentId the ID of the content
     * @return list of ContentFile entities
     */
    List<ContentFile> listFiles(UUID contentId);

    /**
     * Retrieve a file metadata by ID.
     *
     * @param id the file ID
     * @return the ContentFile entity
     */
    ContentFile getFile(UUID id);

    /**
     * Delete a file from storage and database by ID.
     *
     * @param id the file ID
     */
    void deleteFile(UUID id);

    ContentFile updateFile(UUID id, ContentFileUpdateRequest request);
}