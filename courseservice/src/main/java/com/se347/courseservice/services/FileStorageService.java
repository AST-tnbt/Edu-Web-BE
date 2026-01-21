package com.se347.courseservice.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    
    /**
     * Upload thumbnail image to MinIO
     * 
     * @param file the thumbnail file to upload
     * @return the public URL of the uploaded thumbnail
     */
    String uploadThumbnail(MultipartFile file);
}
