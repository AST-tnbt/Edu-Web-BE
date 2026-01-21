package com.se347.courseservice.services.impl;

import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.services.FileStorageService;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    private final MinioClient minioClient;
    
    @Value("${minio.bucket}")
    private String bucketName;
    
    private String minioUrl = "http://localhost:9000";
    
    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("Created MinIO bucket: {}", bucketName);
            }
            
            // Set bucket policy to public read
            String policy = """
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": {"AWS": "*"},
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                        }
                    ]
                }
                """.formatted(bucketName);
            
            minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build()
            );
            logger.info("Set public read policy for bucket: {}", bucketName);
        } catch (Exception e) {
            logger.error("Failed to initialize MinIO bucket", e);
            throw new RuntimeException("Failed to initialize file storage", e);
        }
    }
    
    @Override
    public String uploadThumbnail(MultipartFile file) {
        // Validate file
        validateFile(file);
        
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            String filename = "thumbnails/" + UUID.randomUUID() + extension;
            
            // Upload to MinIO
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            // Return public URL
            String publicUrl = minioUrl + "/" + bucketName + "/" + filename;
            logger.info("Uploaded thumbnail: {}", publicUrl);
            return publicUrl;
            
        } catch (Exception e) {
            logger.error("Failed to upload thumbnail", e);
            throw new CourseException.InvalidRequestException("Failed to upload thumbnail: " + e.getMessage());
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CourseException.InvalidRequestException("Thumbnail file is required");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CourseException.InvalidRequestException(
                "Thumbnail file size exceeds maximum allowed size of 5MB"
            );
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new CourseException.InvalidRequestException(
                "Invalid thumbnail file type. Allowed types: JPEG, PNG, WebP, GIF"
            );
        }
    }
}
