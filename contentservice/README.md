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
| `id`          | BIGINT (PK)  | Mã định danh file                        |
| `content_id`  | BIGINT (FK)  | Liên kết đến content trong CourseService |
| `file_name`   | VARCHAR(255) | Tên file gốc                            |
| `file_path`   | VARCHAR(500) | Đường dẫn file trong storage            |
| `file_size`   | BIGINT       | Kích thước file (bytes)                  |
| `mime_type`   | VARCHAR(100) | Loại file (video/mp4, image/jpeg...)     |
| `status`      | ENUM         | Trạng thái (`UPLOADING`, `PROCESSING`, `READY`, `ERROR`) |
| `created_at`  | TIMESTAMP    | Ngày tạo                                 |
| `updated_at`  | TIMESTAMP    | Ngày cập nhật                            |

### 2. `video_processing_jobs`
Theo dõi các job xử lý video.

| Column        | Type         | Description                              |
|---------------|--------------|------------------------------------------|
| `id`          | BIGINT (PK)  | Mã định danh job                         |
| `content_file_id` | BIGINT (FK) | Liên kết đến content_files              |
| `job_type`    | ENUM         | Loại job (`TRANSCODE`, `THUMBNAIL`, `SUBTITLE`) |
| `status`      | ENUM         | Trạng thái (`PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`) |
| `progress`    | INT          | Tiến độ xử lý (%)                       |
| `result_url`  | VARCHAR(500) | URL kết quả sau xử lý                   |
| `error_message` | TEXT        | Thông báo lỗi (nếu có)                  |
| `created_at`  | TIMESTAMP    | Ngày tạo job                            |
| `completed_at`| TIMESTAMP    | Ngày hoàn thành                         |

### 3. `content_access_logs`
Log truy cập content để analytics.

| Column        | Type         | Description                              |
|---------------|--------------|------------------------------------------|
| `id`          | BIGINT (PK)  | Mã định danh log                         |
| `content_id`  | BIGINT       | ID content được truy cập                |
| `user_id`     | UUID         | ID người dùng truy cập                   |
| `access_type` | ENUM         | Loại truy cập (`STREAM`, `DOWNLOAD`, `PREVIEW`) |
| `ip_address`  | VARCHAR(45)  | IP address người dùng                   |
| `user_agent`  | TEXT         | User agent string                       |
| `accessed_at` | TIMESTAMP    | Thời gian truy cập                      |

---

## 🏗️ Service Architecture

### 1. Controller Layer
Cung cấp các API RESTful cho ContentService:
- `FileUploadController` — Upload và quản lý files
- `ContentStreamController` — Streaming content
- `ProcessingController` — Quản lý video processing jobs
- `AnalyticsController` — Media analytics và reports

### 2. Service Layer
Chứa logic nghiệp vụ của ContentService:
- `FileStorageService`: Quản lý upload/download, storage optimization
- `VideoProcessingService`: Xử lý video với FFmpeg
- `ContentDeliveryService`: CDN integration, streaming protocols
- `AnalyticsService`: Content engagement analytics

### 3. Repository Layer
Chịu trách nhiệm truy vấn và giao tiếp với cơ sở dữ liệu (JPA).

Ví dụ:
```java
public interface ContentFileRepository extends JpaRepository<ContentFile, Long> {
    List<ContentFile> findByContentId(Long contentId);
    List<ContentFile> findByStatus(ProcessingStatus status);
}
```

### 4. Integration Layer
Giao tiếp với các services khác:
- `CourseServiceClient`: Lấy metadata content từ CourseService
- `EnrollmentServiceClient`: Kiểm tra quyền truy cập
- `UserServiceClient`: Lấy thông tin người dùng

---

## 🔐 Authorization Flow

### 1. Access Control
- **Content Access**: Kiểm tra enrollment qua EnrollmentService
- **Upload Permission**: Chỉ instructor của course mới upload được
- **Processing Jobs**: Chỉ owner của content mới quản lý được

### 2. Security Measures
- **Signed URLs**: Sử dụng signed URLs cho content delivery
- **Time-limited Access**: Content URLs có thời hạn
- **IP Restrictions**: Có thể giới hạn theo IP (tùy chọn)

---

## 🔄 Interaction with Other Services

| Service | Purpose | Communication |
|---------|---------|---------------|
| **CourseService** | Lấy metadata content, cập nhật content_url | HTTP/REST + Events |
| **EnrollmentService** | Kiểm tra quyền truy cập content | HTTP/REST |
| **AuthService** | Xác thực JWT, kiểm tra role | HTTP/REST |
| **UserService** | Lấy thông tin người dùng | HTTP/REST |
| **Gateway** | Định tuyến API, load balancing | HTTP/REST |

---

## 📝 Example Workflow

### 1. Instructor upload video
1. Instructor gửi `POST /api/contents/{id}/upload` → ContentService
2. ContentService validate file và tạo ContentFile record
3. ContentService upload file lên S3/MinIO
4. ContentService tạo video processing job
5. ContentService thông báo CourseService về content_url mới

### 2. Video processing
1. Background worker nhận processing job
2. FFmpeg transcode video thành multiple qualities
3. Generate thumbnails và preview images
4. Update job status và result URLs
5. Notify CourseService về completion

### 3. Student xem video
1. Student request content → ContentService
2. ContentService kiểm tra enrollment qua EnrollmentService
3. ContentService generate signed URL cho streaming
4. Student stream content từ CDN
5. Log access cho analytics

---

## 🛠️ Tech Stack

### Core Technologies
- **Spring Boot 3.x**
- **Spring Data JPA**
- **PostgreSQL** (metadata storage)
- **Redis** (caching, job queues)

### File Storage & Processing
- **AWS S3** hoặc **MinIO** (file storage)
- **FFmpeg** (video processing)
- **ImageMagick** (image processing)
- **RabbitMQ/Kafka** (async processing)

### Content Delivery
- **AWS CloudFront** hoặc **CloudFlare** (CDN)
- **HLS/DASH** (streaming protocols)
- **Signed URLs** (secure access)

### Monitoring & Analytics
- **Prometheus** (metrics)
- **Grafana** (dashboards)
- **ELK Stack** (logging)

---

## 🚀 Future Extensions

### Advanced Processing
- AI-powered content analysis
- Automatic subtitle generation
- Content quality assessment
- Smart thumbnail selection

### Enhanced Delivery
- Adaptive bitrate streaming
- Global CDN optimization
- Mobile-optimized delivery
- Offline content support

### Analytics & Insights
- Content engagement heatmaps
- Learning behavior analysis
- Performance optimization recommendations
- A/B testing for content delivery

---

## 🔌 REST API Endpoints

### 📁 File Management

#### Upload File
```http
POST /api/contents/{contentId}/upload
Authorization: Bearer {jwt_token}
Content-Type: multipart/form-data
```
**Request Body:**
```
file: [binary file data]
```

#### Get File Info
```http
GET /api/files/{fileId}
Authorization: Bearer {jwt_token}
```

#### Download File
```http
GET /api/files/{fileId}/download
Authorization: Bearer {jwt_token}
```

#### Delete File
```http
DELETE /api/files/{fileId}
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
