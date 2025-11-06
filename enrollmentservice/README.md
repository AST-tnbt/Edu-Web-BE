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
| `payment_status` | ENUM       | Trạng thái thanh toán (`PENDING`, `PAID`, `REFUNDED`, `CANCELLED`) |
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
