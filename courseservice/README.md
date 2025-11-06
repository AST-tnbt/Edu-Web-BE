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
