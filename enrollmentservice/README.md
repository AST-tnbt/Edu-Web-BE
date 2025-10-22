# EnrollmentService

## 📋 Overview

**EnrollmentService** chịu trách nhiệm quản lý ghi danh và theo dõi tiến độ học tập trong hệ thống học trực tuyến.  
Bao gồm: enrollment management, progress tracking, learning analytics, và student performance reports.

EnrollmentService là một phần của hệ thống microservices, giao tiếp với:
- **CourseService** (metadata khóa học và cấu trúc)
- **ContentService** (kiểm tra quyền truy cập content)
- **AuthService** (xác thực & vai trò)
- **UserService** (thông tin người dùng)

---

## 🏗️ EnrollmentService Architecture

### 1. **Core Responsibilities**
- **Enrollment Management**: Ghi danh, hủy ghi danh, quản lý enrollment status
- **Progress Tracking**: Theo dõi tiến độ học tập, completion status
- **Learning Analytics**: Phân tích hành vi học tập, performance metrics
- **Access Control**: Kiểm tra quyền truy cập content và course

### 2. **Service Boundaries**
- **Input**: Enrollment requests, progress updates từ ContentService
- **Output**: Enrollment status, progress data, analytics reports
- **Events**: Enrollment events, progress milestones
- **Integration**: Real-time sync với CourseService và ContentService

---

## 🗄️ Database Schema

### 1. `enrollments`
Bảng chính quản lý ghi danh học viên.

| Column        | Type         | Description                              |
|---------------|--------------|------------------------------------------|
| `id`          | BIGINT (PK)  | Mã định danh enrollment                   |
| `course_id`   | BIGINT       | ID khóa học (tham chiếu CourseService)   |
| `student_id`  | UUID         | ID học viên (tham chiếu UserService)     |
| `enrolled_at` | TIMESTAMP    | Ngày ghi danh                           |
| `status`      | ENUM         | Trạng thái (`ACTIVE`, `COMPLETED`, `SUSPENDED`, `CANCELLED`) |
| `payment_status` | ENUM       | Trạng thái thanh toán (`PENDING`, `PAID`, `REFUNDED`) |
| `access_expires_at` | TIMESTAMP | Ngày hết hạn truy cập (nếu có)        |
| `created_at`  | TIMESTAMP    | Ngày tạo                                |
| `updated_at`  | TIMESTAMP    | Ngày cập nhật                           |

### 2. `learning_progress`
Theo dõi tiến độ học tập chi tiết.

| Column        | Type         | Description                              |
|---------------|--------------|------------------------------------------|
| `id`          | BIGINT (PK)  | Mã định danh progress                    |
| `enrollment_id` | BIGINT (FK) | Liên kết đến enrollments                |
| `content_id`  | BIGINT       | ID content đã học (tham chiếu CourseService) |
| `lesson_id`   | BIGINT       | ID lesson đã học                          |
| `progress_percentage` | FLOAT    | Phần trăm hoàn thành content (%)         |
| `time_spent`  | INT          | Thời gian học (seconds)                 |
| `is_completed`| BOOLEAN      | Đã hoàn thành content chưa               |
| `last_accessed_at` | TIMESTAMP | Lần cuối truy cập                      |
| `completed_at`| TIMESTAMP    | Ngày hoàn thành (nếu có)                 |

### 3. `course_progress`
Tổng hợp tiến độ của toàn bộ khóa học.

| Column        | Type         | Description                              |
|---------------|--------------|------------------------------------------|
| `id`          | BIGINT (PK)  | Mã định danh course progress             |
| `enrollment_id` | BIGINT (FK) | Liên kết đến enrollments                |
| `overall_progress` | FLOAT    | Tổng tiến độ khóa học (%)               |
| `lessons_completed` | INT      | Số bài học đã hoàn thành                |
| `total_lessons` | INT        | Tổng số bài học                         |
| `contents_completed` | INT      | Số content đã hoàn thành                |
| `total_contents` | INT         | Tổng số content                          |
| `is_course_completed` | BOOLEAN | Đã hoàn thành khóa học chưa            |
| `course_completed_at` | TIMESTAMP | Ngày hoàn thành khóa học               |
| `updated_at`  | TIMESTAMP    | Ngày cập nhật                           |

### 4. `learning_analytics`
Dữ liệu analytics cho phân tích học tập.

| Column        | Type         | Description                              |
|---------------|--------------|------------------------------------------|
| `id`          | BIGINT (PK)  | Mã định danh analytics                   |
| `enrollment_id` | BIGINT (FK) | Liên kết đến enrollments                |
| `learning_session_id` | VARCHAR(100) | ID session học tập                      |
| `session_start` | TIMESTAMP  | Thời gian bắt đầu session               |
| `session_end` | TIMESTAMP    | Thời gian kết thúc session              |
| `total_time_spent` | INT        | Tổng thời gian học (seconds)            |
| `content_accessed` | JSON      | Danh sách content đã truy cập           |
| `interaction_events` | JSON     | Các sự kiện tương tác (play, pause, seek) |
| `device_info` | JSON         | Thông tin thiết bị học tập              |
| `created_at`  | TIMESTAMP    | Ngày tạo                                |

---

## 🏗️ Service Architecture

### 1. Controller Layer
Cung cấp các API RESTful cho EnrollmentService:
- `EnrollmentController` — Quản lý ghi danh
- `ProgressController` — Theo dõi tiến độ học tập
- `AnalyticsController` — Learning analytics và reports
- `AccessController` — Kiểm tra quyền truy cập

### 2. Service Layer
Chứa logic nghiệp vụ của EnrollmentService:
- `EnrollmentService`: Quản lý ghi danh, enrollment lifecycle
- `ProgressService`: Theo dõi và cập nhật tiến độ học tập
- `AnalyticsService`: Phân tích dữ liệu học tập
- `AccessControlService`: Kiểm tra quyền truy cập content

### 3. Repository Layer
Chịu trách nhiệm truy vấn và giao tiếp với cơ sở dữ liệu (JPA).

Ví dụ:
```java
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(UUID studentId);
    List<Enrollment> findByCourseId(Long courseId);
    Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId, Long courseId);
}
```

### 4. Integration Layer
Giao tiếp với các services khác:
- `CourseServiceClient`: Lấy thông tin khóa học và cấu trúc
- `ContentServiceClient`: Kiểm tra quyền truy cập content
- `UserServiceClient`: Lấy thông tin người dùng

---

## 🔐 Authorization Flow

### 1. Enrollment Access Control
- **Student**: Chỉ có thể ghi danh vào khóa học công khai hoặc được mời
- **Instructor**: Có thể xem enrollment của khóa học mình dạy
- **Admin**: Có thể quản lý tất cả enrollments

### 2. Progress Access Control
- **Student**: Chỉ xem được tiến độ của mình
- **Instructor**: Xem được tiến độ học viên trong khóa học mình dạy
- **Admin**: Xem được tất cả progress data

---

## 🔄 Interaction with Other Services

| Service | Purpose | Communication |
|---------|---------|---------------|
| **CourseService** | Lấy metadata khóa học, cấu trúc lessons/contents | HTTP/REST + Events |
| **ContentService** | Kiểm tra quyền truy cập, cập nhật progress | HTTP/REST + Events |
| **AuthService** | Xác thực JWT, kiểm tra role | HTTP/REST |
| **UserService** | Lấy thông tin người dùng | HTTP/REST |
| **Gateway** | Định tuyến API, load balancing | HTTP/REST |

---

## 📝 Example Workflow

### 1. Student ghi danh khóa học
1. Student gửi `POST /api/courses/{courseId}/enroll` → EnrollmentService
2. EnrollmentService kiểm tra quyền truy cập qua AuthService
3. EnrollmentService tạo enrollment record
4. EnrollmentService thông báo CourseService về enrollment mới
5. EnrollmentService tạo course_progress record

### 2. Student học content
1. Student truy cập content → ContentService
2. ContentService kiểm tra enrollment qua EnrollmentService
3. Nếu có quyền → ContentService stream content
4. ContentService gửi progress update đến EnrollmentService
5. EnrollmentService cập nhật learning_progress

### 3. Theo dõi tiến độ
1. Student xem progress → EnrollmentService
2. EnrollmentService tính toán overall progress
3. EnrollmentService trả về detailed progress data
4. Nếu hoàn thành khóa học → trigger completion events

---

## 🛠️ Tech Stack

### Core Technologies
- **Spring Boot 3.x**
- **Spring Data JPA**
- **PostgreSQL** (main database)
- **Redis** (caching, session management)

### Analytics & Processing
- **Elasticsearch** (advanced analytics queries)
- **Apache Kafka** (event streaming)
- **Apache Spark** (big data processing)
- **Prometheus + Grafana** (monitoring)

### Integration
- **OpenFeign** (service communication)
- **Spring Cloud** (service discovery)
- **RabbitMQ** (async messaging)

---

## 🚀 Future Extensions

### Advanced Analytics
- Machine learning cho personalized learning paths
- Predictive analytics cho student success
- Behavioral pattern analysis
- Real-time learning recommendations

### Gamification
- Achievement system
- Learning streaks
- Social learning features
- Leaderboards và competitions

### Adaptive Learning
- Dynamic content recommendations
- Difficulty adjustment based on progress
- Personalized learning schedules
- AI-powered tutoring assistance

---

## 🔌 REST API Endpoints

### 🎓 Enrollment Management

#### Enroll in Course
```http
POST /api/courses/{courseId}/enroll
Authorization: Bearer {jwt_token}
```

#### Get User Enrollments
```http
GET /api/enrollments/my-courses
Authorization: Bearer {jwt_token}
```

#### Get Course Enrollments
```http
GET /api/courses/{courseId}/enrollments
Authorization: Bearer {jwt_token}
```

#### Update Enrollment Status
```http
PUT /api/enrollments/{enrollmentId}/status
Authorization: Bearer {jwt_token}
```
**Request Body:**
```json
{
  "status": "SUSPENDED",
  "reason": "Payment pending"
}
```

#### Cancel Enrollment
```http
DELETE /api/enrollments/{enrollmentId}
Authorization: Bearer {jwt_token}
```

---

### 📊 Progress Tracking

#### Get Student Progress
```http
GET /api/courses/{courseId}/progress
Authorization: Bearer {jwt_token}
```

#### Update Content Progress
```http
PUT /api/progress/content/{contentId}
Authorization: Bearer {jwt_token}
```
**Request Body:**
```json
{
  "progressPercentage": 75.5,
  "timeSpent": 1200,
  "isCompleted": false
}
```

#### Mark Content as Completed
```http
POST /api/progress/content/{contentId}/complete
Authorization: Bearer {jwt_token}
```

#### Get Progress History
```http
GET /api/progress/history
Authorization: Bearer {jwt_token}
```

---

### 📈 Learning Analytics

#### Get Learning Analytics
```http
GET /api/analytics/learning
Authorization: Bearer {jwt_token}
```

#### Get Course Analytics
```http
GET /api/courses/{courseId}/analytics
Authorization: Bearer {jwt_token}
```

#### Get Student Performance
```http
GET /api/students/{studentId}/performance
Authorization: Bearer {jwt_token}
```

#### Get Learning Sessions
```http
GET /api/analytics/sessions
Authorization: Bearer {jwt_token}
```

---

### 🔐 Access Control

#### Check Content Access
```http
GET /api/access/content/{contentId}
Authorization: Bearer {jwt_token}
```

#### Check Course Access
```http
GET /api/access/course/{courseId}
Authorization: Bearer {jwt_token}
```

#### Get Access Permissions
```http
GET /api/access/permissions
Authorization: Bearer {jwt_token}
```

---

## 📁 Folder Structure

```
enrollment-service/
├── src/main/java/com/se347/enrollmentservice
│   ├── controller/
│   │   ├── EnrollmentController.java
│   │   ├── ProgressController.java
│   │   ├── AnalyticsController.java
│   │   └── AccessController.java
│   ├── service/
│   │   ├── EnrollmentService.java
│   │   ├── ProgressService.java
│   │   ├── AnalyticsService.java
│   │   └── AccessControlService.java
│   ├── client/
│   │   ├── CourseServiceClient.java
│   │   ├── ContentServiceClient.java
│   │   └── UserServiceClient.java
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── entities/
│   │   ├── Enrollment.java
│   │   ├── LearningProgress.java
│   │   ├── CourseProgress.java
│   │   └── LearningAnalytics.java
│   ├── repository/
│   │   ├── EnrollmentRepository.java
│   │   ├── LearningProgressRepository.java
│   │   ├── CourseProgressRepository.java
│   │   └── LearningAnalyticsRepository.java
│   ├── analytics/
│   │   ├── ProgressCalculator.java
│   │   ├── LearningPatternAnalyzer.java
│   │   └── PerformanceMetrics.java
│   ├── events/
│   │   ├── EnrollmentEventHandler.java
│   │   ├── ProgressEventHandler.java
│   │   └── AnalyticsEventHandler.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── FeignConfig.java
│   │   └── AnalyticsConfig.java
│   ├── exception/
│   │   ├── EnrollmentNotFoundException.java
│   │   ├── AccessDeniedException.java
│   │   └── GlobalExceptionHandler.java
│   └── EnrollmentServiceApplication.java
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
- **STUDENT**: Can enroll, view own progress, access enrolled content
- **INSTRUCTOR**: Can view enrollments and progress for their courses
- **ADMIN**: Full access to all enrollment data and analytics
