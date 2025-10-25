# ContentService

## 📋 Overview

**ContentService** chịu trách nhiệm quản lý toàn bộ media content trong hệ thống học trực tuyến.  
Bao gồm: upload/download files, video processing, content delivery, streaming, và media analytics.

ContentService là một phần của hệ thống microservices, giao tiếp với:
- **CourseService** (metadata và cấu trúc khóa học)
- **EnrollmentService** (kiểm tra quyền truy cập)
- **AuthService** (xác thực & vai trò)
- **UserService** (thông tin người dùng)

---

## 🏗️ ContentService Architecture

### 1. **Core Responsibilities**
- **File Management**: Upload, download, storage optimization
- **Video Processing**: Transcoding, thumbnail generation, quality optimization
- **Content Delivery**: CDN integration, streaming protocols
- **Media Analytics**: Content engagement, performance metrics

### 2. **Service Boundaries**
- **Input**: File uploads, processing requests từ CourseService
- **Output**: Optimized content URLs, streaming endpoints
- **Storage**: AWS S3/MinIO integration
- **Processing**: Async video processing với FFmpeg

---

## 🗄️ Database Schema

### 1. `content_files`
Lưu trữ thông tin về files đã upload và processed.

| Column        | Type         | Description                              |
|---------------|--------------|------------------------------------------|
| `id`          | UUID (PK)    | Mã định danh file                        |
| `content_id`  | UUID (FK)    | Liên kết đến content trong CourseService |
| `file_name`   | VARCHAR(255) | Tên file gốc                            |
| `file_path`   | VARCHAR(500) | Đường dẫn file trong storage            |
| `file_size`   | BIGINT       | Kích thước file (bytes)                  |
| `mime_type`   | VARCHAR(100) | Loại file (video/mp4, image/jpeg...)     |
| `status`      | ENUM         | Trạng thái (`UPLOADING`, `PROCESSING`, `READY`, `ERROR`) |
| `created_at`  | TIMESTAMP    | Ngày tạo                                 |
| `updated_at`  | TIMESTAMP    | Ngày cập nhật                            |

---

## 🔌 REST API Endpoints

### 📁 File Management

#### Upload File
```http
POST /api/content-files/upload/{contentId}
Authorization: Bearer {jwt_token}
Content-Type: multipart/form-data
```
**Request Body:**
```
file: [pdf/mp4]
```

#### Get File
```http
GET /api/content-files/{fileId}
Authorization: Bearer {jwt_token}
```

#### Get list file of content
```http
GET /api/content-files/list/{contentId}
Authorization: Bearer {jwt_token}
```

#### Update File 
```http
PATCH /api/content-files/{fileId}
Authorization: Bearer {jwt_token}
Request body: fileName or status
```

#### Delete File
```http
DELETE /api/content-files/{fileId}
Authorization: Bearer {jwt_token}
```

---

### 🎥 Video Processing

#### Get Processing Jobs
```http
GET /api/contents/{contentId}/jobs
Authorization: Bearer {jwt_token}
```

#### Create Processing Job
```http
POST /api/contents/{contentId}/process
Authorization: Bearer {jwt_token}
```
**Request Body:**
```json
{
  "jobType": "TRANSCODE",
  "quality": "HD",
  "format": "MP4"
}
```

#### Get Job Status
```http
GET /api/jobs/{jobId}
Authorization: Bearer {jwt_token}
```

---

### 📺 Content Streaming

#### Get Stream URL
```http
GET /api/contents/{contentId}/stream
Authorization: Bearer {jwt_token}
```

#### Get Thumbnail
```http
GET /api/contents/{contentId}/thumbnail
Authorization: Bearer {jwt_token}
```

#### Get Preview
```http
GET /api/contents/{contentId}/preview
Authorization: Bearer {jwt_token}
```

---

### 📊 Analytics

#### Get Content Analytics
```http
GET /api/contents/{contentId}/analytics
Authorization: Bearer {jwt_token}
```

#### Get Access Logs
```http
GET /api/contents/{contentId}/logs
Authorization: Bearer {jwt_token}
```

#### Get Performance Metrics
```http
GET /api/analytics/performance
Authorization: Bearer {jwt_token}
```

---

## 📁 Folder Structure

```
content-service/
├── src/main/java/com/se347/contentservice
│   ├── controller/
│   │   ├── FileUploadController.java
│   │   ├── ContentStreamController.java
│   │   ├── ProcessingController.java
│   │   └── AnalyticsController.java
│   ├── service/
│   │   ├── FileStorageService.java
│   │   ├── VideoProcessingService.java
│   │   ├── ContentDeliveryService.java
│   │   └── AnalyticsService.java
│   ├── client/
│   │   ├── CourseServiceClient.java
│   │   ├── EnrollmentServiceClient.java
│   │   └── UserServiceClient.java
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── entities/
│   │   ├── ContentFile.java
│   │   ├── VideoProcessingJob.java
│   │   └── ContentAccessLog.java
│   ├── repository/
│   │   ├── ContentFileRepository.java
│   │   ├── VideoProcessingJobRepository.java
│   │   └── ContentAccessLogRepository.java
│   ├── processor/
│   │   ├── VideoProcessor.java
│   │   ├── ImageProcessor.java
│   │   └── ThumbnailGenerator.java
│   ├── storage/
│   │   ├── S3StorageService.java
│   │   ├── MinIOStorageService.java
│   │   └── CDNService.java
│   ├── config/
│   │   ├── StorageConfig.java
│   │   ├── ProcessingConfig.java
│   │   └── CDNConfig.java
│   ├── exception/
│   │   ├── FileNotFoundException.java
│   │   ├── ProcessingException.java
│   │   └── GlobalExceptionHandler.java
│   └── ContentServiceApplication.java
└── src/main/resources/
    ├── application.yml
    └── schema.sql
```

---

## 🔐 Authentication & Authorization

All endpoints require JWT authentication:
```http
Authorization: Bearer {jwt_token}
```

**Role-based Access:**
- **STUDENT**: Can stream/download content they're enrolled in
- **INSTRUCTOR**: Can upload/manage content for their courses
- **ADMIN**: Full access to all content and analytics
