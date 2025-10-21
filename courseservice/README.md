# CourseService

## 📋 Overview

**CourseService** chịu trách nhiệm quản lý cấu trúc và metadata của khóa học trong hệ thống.  
Bao gồm: quản lý khóa học, danh mục (category), bài học (lesson), và cấu trúc nội dung học.

CourseService là một phần của hệ thống microservices, giao tiếp với:
- **AuthService** (xác thực & vai trò)
- **UserService** (thông tin chi tiết người dùng)
- **ContentService** (quản lý file và media)
- **EnrollmentService** (quản lý ghi danh và tiến độ học tập)

## 🏗️ CourseService Architecture

**CourseService** tập trung vào core business logic:

### **Core Responsibilities**
- Quản lý khóa học và cấu trúc
- Quản lý danh mục (categories)
- Quản lý bài học (lessons)
- Quản lý metadata của nội dung
- Cung cấp APIs cho Course Management

---

## 🗄️ Database Schema

### 1. `categories`
Chứa thông tin danh mục của các khóa học (ví dụ: Lập trình, Marketing, Thiết kế...).

| Column        | Type         | Description                              |
|---------------|--------------|------------------------------------------|
| `id`          | BIGINT (PK)  | Mã định danh duy nhất của category       |
| `name`        | VARCHAR(100) | Tên danh mục                             |
| `description` | TEXT         | Mô tả chi tiết danh mục                  |
| `created_at`  | TIMESTAMP    | Ngày tạo                                 |
| `updated_at`  | TIMESTAMP    | Ngày cập nhật                            |

### 2. `courses`
Chứa thông tin chính của từng khóa học.

| Column          | Type          | Description                                             |
|-----------------|---------------|---------------------------------------------------------|
| `id`            | BIGINT (PK)   | Mã định danh khóa học                                   |
| `title`         | VARCHAR(255)  | Tên khóa học                                            |
| `description`   | TEXT          | Mô tả chi tiết nội dung khóa học                        |
| `thumbnail_url` | VARCHAR(255)  | Ảnh đại diện khóa học                                  |
| `price`         | DECIMAL(10,2) | Giá khóa học                                            |
| `level`         | VARCHAR(50)   | Mức độ (Beginner, Intermediate, Advanced)              |
| `category_id`   | BIGINT (FK)   | Liên kết đến bảng `categories`                         |
| `instructor_id` | UUID          | ID người tạo khóa học (tham chiếu đến UserService)      |
| `created_at`    | TIMESTAMP     | Ngày tạo khóa học                                      |
| `updated_at`    | TIMESTAMP     | Ngày cập nhật                                          |

### 3. `lessons`
Mỗi khóa học bao gồm nhiều bài học.

| Column        | Type         | Description                              |
|---------------|--------------|------------------------------------------|
| `id`          | BIGINT (PK)  | Mã định danh bài học                     |
| `course_id`   | BIGINT (FK)  | Liên kết đến bảng `courses`              |
| `title`       | VARCHAR(255) | Tên bài học                              |
| `order_index` | INT          | Thứ tự bài học trong khóa                |
| `created_at`  | TIMESTAMP    | Ngày tạo                                 |
| `updated_at`  | TIMESTAMP    | Ngày cập nhật                            |

### 4. `contents` (Metadata only - ContentService handles actual files)
Mỗi bài học có thể có nhiều nội dung (video, text, quiz...).

| Column        | Type         | Description                              |
|---------------|--------------|------------------------------------------|
| `id`          | BIGINT (PK)  | Mã định danh nội dung                    |
| `lesson_id`   | BIGINT (FK)  | Liên kết đến bảng `lessons`              |
| `type`        | ENUM          | Loại nội dung (`VIDEO`, `TEXT`, `QUIZ`)  |
| `title`       | VARCHAR(255) | Tiêu đề nội dung                         |
| `content_url` | VARCHAR(255) | Link video hoặc file nội dung (managed by ContentService) |
| `text_content`| TEXT         | Nội dung văn bản (nếu type=TEXT)         |
| `order_index` | INT          | Thứ tự hiển thị nội dung trong bài học   |
| `status`      | ENUM          | Trạng thái (`DRAFT`, `PROCESSING`, `READY`, `ERROR`) |

**Note:** Bảng `enrollments` đã được chuyển sang **EnrollmentService**

---

## 🏗️ CourseService Architecture

### 1. Controller Layer
Cung cấp các API RESTful cho CourseService:
- `CourseController` — Quản lý khóa học (tạo, xem, cập nhật, xóa)
- `CategoryController` — Quản lý danh mục
- `LessonController` — Quản lý bài học
- `ContentMetadataController` — Quản lý metadata nội dung (không xử lý file)

### 2. Service Layer
Chứa logic nghiệp vụ của CourseService:
- `CourseService`: Xử lý tạo/sửa/xóa khóa học, gán giảng viên, lấy danh sách khóa học theo category
- `LessonService`: Thêm/sửa bài học, sắp xếp thứ tự
- `ContentMetadataService`: Quản lý metadata nội dung (không xử lý file thực tế)
- `CategoryService`: Quản lý danh mục khóa học

### 3. Repository Layer
Chịu trách nhiệm truy vấn và giao tiếp với cơ sở dữ liệu (JPA).

Ví dụ:
```java
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCategoryId(Long categoryId);
    List<Course> findByInstructorId(UUID instructorId);
}
```

### 4. Integration Layer
Giao tiếp với các services khác:
- `ContentServiceClient`: Gọi ContentService để xử lý file
- `EnrollmentServiceClient`: Gọi EnrollmentService để quản lý ghi danh
- `UserServiceClient`: Lấy thông tin người dùng

---

## 🔐 Authorization Flow

### 1. Role Management
- AuthService chỉ định 2 vai trò cơ bản: **USER**, **ADMIN**
- Mỗi người dùng trong UserService có thêm trường `role_context` (ví dụ: INSTRUCTOR, STUDENT)
- Một người có thể vừa là INSTRUCTOR trong khóa này, vừa là STUDENT trong khóa khác

### 2. Access Control
- Chỉ `instructor_id` mới có quyền chỉnh sửa khóa học, bài học, nội dung
- Học viên (`student_id`) chỉ được truy cập những khóa học mà họ đã ghi danh (enrollments)
- **ADMIN** có thể truy cập toàn bộ

---

## 🔄 Interaction with Other Services

| Service | Purpose | Communication |
|---------|---------|---------------|
| **AuthService** | Xác thực JWT, xác minh token, kiểm tra role | HTTP/REST |
| **UserService** | Lấy thông tin chi tiết người dùng (giảng viên / học viên) | HTTP/REST |
| **ContentService** | Quản lý file, video processing, content delivery | HTTP/REST + Events |
| **EnrollmentService** | Quản lý ghi danh, tiến độ học tập | HTTP/REST + Events |
| **Gateway** | Định tuyến API, xác thực header HMAC hoặc JWT | HTTP/REST |

---

## 📝 Example Workflow

### 1. Instructor tạo khóa học
1. Instructor gửi request `POST /courses` → CourseService
2. Gateway xác thực JWT → gửi request đến CourseService
3. CourseService lưu thông tin vào bảng courses với instructor_id
4. CourseService tạo cấu trúc lesson và content metadata

### 2. Instructor upload nội dung
1. Instructor gửi `POST /courses/{id}/lessons/{lessonId}/contents` → CourseService
2. CourseService tạo content metadata
3. CourseService gọi ContentService để upload file
4. ContentService xử lý file và trả về URL
5. CourseService cập nhật content_url

### 3. Student ghi danh khóa học
1. Student gửi request `POST /courses/{id}/enroll` → EnrollmentService
2. EnrollmentService kiểm tra quyền truy cập
3. EnrollmentService tạo enrollment record
4. EnrollmentService thông báo cho CourseService về enrollment mới

### 4. Student học nội dung
1. Student truy cập content → ContentService
2. ContentService kiểm tra quyền truy cập qua EnrollmentService
3. ContentService stream nội dung cho student
4. EnrollmentService cập nhật tiến độ học tập

---

## 🛠️ Tech Stack

- **Spring Boot 3.x**
- **Spring Data JPA**
- **PostgreSQL** (hoặc MySQL)
- **Spring Security** (JWT validation)
- **OpenFeign** (giao tiếp với các services khác)
- **Lombok**
- **MapStruct** (mapping DTO ↔ Entity)
- **Spring Cloud** (service discovery, load balancing)

---

## 🚀 Future Extensions

- Thêm hệ thống đánh giá (rating, review)
- Gợi ý khóa học theo danh mục hoặc hành vi học viên
- Advanced search và filtering
- Course versioning và history
- Course templates và cloning
- Bulk course operations

---

## 🔌 REST API Endpoints

### 📚 Course Management

#### Get All Courses
```http
GET /api/courses
```
**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)
- `categoryId` (optional): Filter by category
- `level` (optional): Filter by level (BEGINNER, INTERMEDIATE, ADVANCED)
- `instructorId` (optional): Filter by instructor
- `search` (optional): Search by title or description

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Spring Boot Fundamentals",
      "description": "Learn Spring Boot from scratch",
      "thumbnailUrl": "https://example.com/thumbnail.jpg",
      "price": 99.99,
      "level": "BEGINNER",
      "category": {
        "id": 1,
        "name": "Programming"
      },
      "instructorId": "123e4567-e89b-12d3-a456-426614174000",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

#### Get Course by ID
```http
GET /api/courses/{id}
```

#### Create Course
```http
POST /api/courses
Authorization: Bearer {jwt_token}
```
**Request Body:**
```json
{
  "title": "Advanced Java Programming",
  "description": "Master advanced Java concepts",
  "thumbnailUrl": "https://example.com/thumbnail.jpg",
  "price": 199.99,
  "level": "ADVANCED",
  "categoryId": 1
}
```

#### Update Course
```http
PUT /api/courses/{id}
Authorization: Bearer {jwt_token}
```

#### Delete Course
```http
DELETE /api/courses/{id}
Authorization: Bearer {jwt_token}
```

#### Get Courses by Instructor
```http
GET /api/courses/instructor/{instructorId}
```

#### Get Courses by Category
```http
GET /api/courses/category/{categoryId}
```

---

### 📂 Category Management

#### Get All Categories
```http
GET /api/categories
```

#### Get Category by ID
```http
GET /api/categories/{id}
```

#### Create Category
```http
POST /api/categories
Authorization: Bearer {jwt_token}
```
**Request Body:**
```json
{
  "name": "Data Science",
  "description": "Courses related to data science and analytics"
}
```

#### Update Category
```http
PUT /api/categories/{id}
Authorization: Bearer {jwt_token}
```

#### Delete Category
```http
DELETE /api/categories/{id}
Authorization: Bearer {jwt_token}
```

---

### 📖 Lesson Management

#### Get Lessons by Course
```http
GET /api/courses/{courseId}/lessons
```

#### Get Lesson by ID
```http
GET /api/lessons/{id}
```

#### Create Lesson
```http
POST /api/courses/{courseId}/lessons
Authorization: Bearer {jwt_token}
```
**Request Body:**
```json
{
  "title": "Introduction to Spring Boot",
  "orderIndex": 1
}
```

#### Update Lesson
```http
PUT /api/lessons/{id}
Authorization: Bearer {jwt_token}
```

#### Delete Lesson
```http
DELETE /api/lessons/{id}
Authorization: Bearer {jwt_token}
```

#### Reorder Lessons
```http
PUT /api/courses/{courseId}/lessons/reorder
Authorization: Bearer {jwt_token}
```
**Request Body:**
```json
{
  "lessonOrders": [
    {"lessonId": 1, "orderIndex": 1},
    {"lessonId": 2, "orderIndex": 2}
  ]
}
```

---

### 📄 Content Metadata Management

#### Get Contents by Lesson
```http
GET /api/lessons/{lessonId}/contents
```

#### Get Content by ID
```http
GET /api/contents/{id}
```

#### Create Content Metadata
```http
POST /api/lessons/{lessonId}/contents
Authorization: Bearer {jwt_token}
```
**Request Body:**
```json
{
  "type": "VIDEO",
  "title": "Spring Boot Setup",
  "textContent": null,
  "orderIndex": 1
}
```

#### Update Content Metadata
```http
PUT /api/contents/{id}
Authorization: Bearer {jwt_token}
```

#### Delete Content
```http
DELETE /api/contents/{id}
Authorization: Bearer {jwt_token}
```

#### Reorder Contents
```http
PUT /api/lessons/{lessonId}/contents/reorder
Authorization: Bearer {jwt_token}
```

#### Upload Content File
```http
POST /api/contents/{id}/upload
Authorization: Bearer {jwt_token}
Content-Type: multipart/form-data
```
**Note:** Endpoint này sẽ forward request đến ContentService

---

### 🎓 Enrollment Management (Forwarded to EnrollmentService)

#### Enroll in Course
```http
POST /api/courses/{courseId}/enroll
Authorization: Bearer {jwt_token}
```
**Note:** Endpoint này sẽ forward request đến EnrollmentService

#### Get User Enrollments
```http
GET /api/enrollments/my-courses
Authorization: Bearer {jwt_token}
```
**Note:** Endpoint này sẽ forward request đến EnrollmentService

#### Get Course Enrollments
```http
GET /api/courses/{courseId}/enrollments
Authorization: Bearer {jwt_token}
```
**Note:** Endpoint này sẽ forward request đến EnrollmentService

#### Update Progress
```http
PUT /api/enrollments/{enrollmentId}/progress
Authorization: Bearer {jwt_token}
```
**Note:** Endpoint này sẽ forward request đến EnrollmentService

#### Mark Content as Completed
```http
POST /api/contents/{contentId}/complete
Authorization: Bearer {jwt_token}
```
**Note:** Endpoint này sẽ forward request đến EnrollmentService

#### Get Student Progress
```http
GET /api/courses/{courseId}/progress
Authorization: Bearer {jwt_token}
```
**Note:** Endpoint này sẽ forward request đến EnrollmentService

---

### 🔍 Search & Filter Endpoints

#### Search Courses
```http
GET /api/courses/search
```
**Query Parameters:**
- `q`: Search query
- `categoryId`: Filter by category
- `level`: Filter by level
- `minPrice`: Minimum price
- `maxPrice`: Maximum price
- `sortBy`: Sort field (title, price, createdAt)
- `sortDirection`: Sort direction (ASC, DESC)

#### Get Popular Courses
```http
GET /api/courses/popular
```

#### Get Recent Courses
```http
GET /api/courses/recent
```

#### Get Courses by Level
```http
GET /api/courses/level/{level}
```

---

### 📊 Analytics Endpoints (Admin Only)

#### Get Course Statistics
```http
GET /api/admin/courses/statistics
Authorization: Bearer {jwt_token}
```

#### Get Category Statistics
```http
GET /api/admin/categories/statistics
Authorization: Bearer {jwt_token}
```

#### Get Instructor Statistics
```http
GET /api/admin/instructors/statistics
Authorization: Bearer {jwt_token}
```

---

### 🔐 Authentication & Authorization

All endpoints (except public ones) require JWT authentication:
```http
Authorization: Bearer {jwt_token}
```

**Role-based Access:**
- **STUDENT**: Can view courses, browse course content
- **INSTRUCTOR**: Can manage their own courses, lessons, and content metadata
- **ADMIN**: Full access to all course management endpoints

---

## 📁 Folder Structure

### CourseService
```
course-service/
├── src/main/java/com/se347/courseservice
│   ├── controller/
│   │   ├── CourseController.java
│   │   ├── CategoryController.java
│   │   ├── LessonController.java
│   │   └── ContentMetadataController.java
│   ├── service/
│   │   ├── CourseService.java
│   │   ├── CategoryService.java
│   │   ├── LessonService.java
│   │   └── ContentMetadataService.java
│   ├── client/
│   │   ├── ContentServiceClient.java
│   │   ├── EnrollmentServiceClient.java
│   │   └── UserServiceClient.java
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── entities/
│   │   ├── Category.java
│   │   ├── Course.java
│   │   ├── Lesson.java
│   │   └── Content.java
│   ├── repository/
│   │   ├── CategoryRepository.java
│   │   ├── CourseRepository.java
│   │   ├── LessonRepository.java
│   │   └── ContentRepository.java
│   ├── exception/
│   │   ├── CourseNotFoundException.java
│   │   ├── UnauthorizedAccessException.java
│   │   └── GlobalExceptionHandler.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── FeignConfig.java
│   │   └── ServiceDiscoveryConfig.java
│   ├── util/
│   │   └── JwtUtil.java
│   └── CourseServiceApplication.java
└── src/main/resources/
    ├── application.yml
    └── schema.sql
```
