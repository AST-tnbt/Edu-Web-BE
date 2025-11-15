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

## 🧪 Postman Test Cases

### 1. Tạo danh mục (`POST /api/categories`)
- **Request**
  - URL: `{{baseUrl}}/api/categories`
  - Headers: `Content-Type: application/json`
  - Body:
    ```json
    {
      "categoryName": "Programming",
      "description": "Các khóa học lập trình chuyên sâu"
    }
    ```
- **Tests**
  ```javascript
  pm.test("Status code 200", () => pm.response.to.have.status(200));
  pm.test("Có categoryName", () => pm.expect(pm.response.json().categoryName).to.eql("Programming"));
  ```

### 2. Lấy tất cả danh mục (`GET /api/categories`)
- **Request**: `{{baseUrl}}/api/categories`
- **Tests**
  ```javascript
  pm.test("Status code 200", () => pm.response.to.have.status(200));
  pm.test("Trả về mảng", () => pm.expect(Array.isArray(pm.response.json())).to.be.true);
  pm.test("Có ít nhất 1 danh mục", () => pm.expect(pm.response.json().length).to.be.above(0));
  ```

### 3. Tạo khóa học (`POST /api/courses`)
- **Request**
  - URL: `{{baseUrl}}/api/courses`
  - Body:
    ```json
    {
      "title": "Spring Boot Microservices",
      "description": "Xây dựng kiến trúc microservices với Spring Boot",
      "thumbnailUrl": "https://cdn.eduweb.com/courses/spring-boot.jpg",
      "price": 49.99,
      "level": "INTERMEDIATE",
      "categoryName": "Programming",
      "instructorId": "{{instructorId}}"
    }
    ```
- **Tests**
  ```javascript
  pm.test("Status code 201", () => pm.response.to.have.status(201));
  pm.test("Có courseId", () => pm.expect(pm.response.json().courseId).to.exist);
  pm.test("Mức giá đúng", () => pm.expect(pm.response.json().price).to.eql(49.99));
  pm.environment.set("courseId", pm.response.json().courseId);
  ```

### 4. Lấy khóa học theo ID (`GET /api/courses/{{courseId}}`)
- **Request**: `{{baseUrl}}/api/courses/{{courseId}}`
- **Tests**
  ```javascript
  pm.test("Status code 200", () => pm.response.to.have.status(200));
  pm.test("Title khớp", () => pm.expect(pm.response.json().title).to.eql("Spring Boot Microservices"));
  ```

### 5. Cập nhật khóa học (`PUT /api/courses/{{courseId}}`)
- **Request**
  - URL: `{{baseUrl}}/api/courses/{{courseId}}`
  - Body:
    ```json
    {
      "title": "Spring Boot Microservices - 2025",
      "description": "Cập nhật nội dung mới nhất cho microservices",
      "thumbnailUrl": "https://cdn.eduweb.com/courses/spring-boot-2025.jpg",
      "price": 59.99,
      "level": "ADVANCED",
      "categoryName": "Programming",
      "instructorId": "{{instructorId}}",
      "courseId": "{{courseId}}"
    }
    ```
- **Tests**
  ```javascript
  pm.test("Status code 200", () => pm.response.to.have.status(200));
  pm.test("Giá mới cập nhật", () => pm.expect(pm.response.json().price).to.eql(59.99));
  ```

### 6. Tạo section (`POST /api/sections`)
- **Request**
  - URL: `{{baseUrl}}/api/sections`
  - Body:
    ```json
    {
      "courseId": "{{courseId}}",
      "title": "Giới thiệu dự án",
      "description": "Tổng quan về dự án mẫu",
      "orderIndex": 1
    }
    ```
- **Tests**
  ```javascript
  pm.test("Status code 200", () => pm.response.to.have.status(200));
  pm.test("Có sectionId", () => pm.expect(pm.response.json().sectionId).to.exist);
  pm.environment.set("sectionId", pm.response.json().sectionId);
  ```

### 7. Tạo lesson (`POST /api/lessons`)
- **Request**
  - URL: `{{baseUrl}}/api/lessons`
  - Body:
    ```json
    {
      "sectionId": "{{sectionId}}",
      "title": "Cấu hình dự án",
      "orderIndex": 1
    }
    ```
- **Tests**
  ```javascript
  pm.test("Status code 200", () => pm.response.to.have.status(200));
  pm.test("Thông tin lesson hợp lệ", () => {
    const body = pm.response.json();
    pm.expect(body.sectionId).to.eql(pm.environment.get("sectionId"));
    pm.expect(body.orderIndex).to.eql(1);
  });
  pm.environment.set("lessonId", pm.response.json().lessonId);
  ```

### 8. Thêm content metadata (`POST /api/contents`)
- **Request**
  - URL: `{{baseUrl}}/api/contents`
  - Body:
    ```json
    {
      "lessonId": "{{lessonId}}",
      "contentType": "VIDEO",
      "title": "Demo kiến trúc",
      "contentUrl": "https://cdn.eduweb.com/videos/demo.mp4",
      "textContent": null,
      "orderIndex": 1,
      "status": "READY"
    }
    ```
- **Tests**
  ```javascript
  pm.test("Status code 200", () => pm.response.to.have.status(200));
  pm.test("Content type là VIDEO", () => pm.expect(pm.response.json().contentType).to.eql("VIDEO"));
  pm.environment.set("contentId", pm.response.json().contentId);
  ```

### 9. Lấy tổng số bài học của khóa (`GET /api/courses/{{courseId}}/total-lessons`)
- **Request**: `{{baseUrl}}/api/courses/{{courseId}}/total-lessons`
- **Tests**
  ```javascript
  pm.test("Status code 200", () => pm.response.to.have.status(200));
  pm.test("Tổng số bài học >= 1", () => pm.expect(pm.response.json()).to.be.at.least(1));
  ```