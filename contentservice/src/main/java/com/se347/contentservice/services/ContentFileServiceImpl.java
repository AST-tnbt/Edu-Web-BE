package com.se347.contentservice.services;

import com.se347.contentservice.dtos.ContentFileUpdateRequest;
import com.se347.contentservice.entities.ContentFile;
import com.se347.contentservice.enums.FileStatus;
import com.se347.contentservice.exception.FileNotFoundException;
import com.se347.contentservice.exception.FileStorageException;
import com.se347.contentservice.exception.InvalidFileTypeException;
import com.se347.contentservice.repositories.ContentFileRepository;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class ContentFileServiceImpl implements ContentFileService {

    private final MinioClient minioClient;
    private final ContentFileRepository repository;

    @Value("${minio.bucket}")
    private String bucketName;

    public ContentFileServiceImpl(MinioClient minioClient, ContentFileRepository repository) {
        this.minioClient = minioClient;
        this.repository = repository;
    }

    public ContentFile uploadFile(MultipartFile file, UUID contentId) {
        String mimeType = file.getContentType();

        if (!List.of("video/mp4", "application/pdf").contains(mimeType)) {
            throw new InvalidFileTypeException("Only MP4 and PDF files are allowed");
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = contentId + "/" + fileName;

        ContentFile entity = new ContentFile();
        entity.setContentId(contentId);
        entity.setFileName(file.getOriginalFilename());
        entity.setFilePath(filePath);
        entity.setFileSize(file.getSize());
        entity.setMimeType(mimeType);
        entity.setStatus(FileStatus.UPLOADING);
        repository.save(entity);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(mimeType)
                            .build()
            );
        } catch (Exception e) {
            entity.setStatus(FileStatus.ERROR);
            repository.save(entity);
            throw new FileStorageException("Failed to upload file to MinIO", e);
        }

        entity.setStatus(FileStatus.READY);
        repository.save(entity);
        return entity;
    }

    public List<ContentFile> listFiles(UUID contentId) {
        return repository.findByContentId(contentId);
    }

    public ContentFile getFile(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with ID: " + id));
    }

    public void deleteFile(UUID id) {
        ContentFile file = repository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with ID: " + id));

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(file.getFilePath())
                            .build()
            );
        } catch (Exception e) {
            throw new FileStorageException("Failed to delete file from MinIO", e);
        }

        repository.delete(file);
    }

    @Override
    public ContentFile updateFile(UUID id, ContentFileUpdateRequest request) {
        ContentFile file = repository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + id));
        if (request.getFileName() != null) {
            file.setFileName(request.getFileName());
        }
        if (request.getStatus() != null) {
            file.setStatus(request.getStatus());
        }
        return repository.save(file);
    }

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
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
        } catch (Exception e) {
            throw new FileStorageException("Failed to initialize MinIO bucket", e);
        }
    }
}

