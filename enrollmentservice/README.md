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

---

## 🧪 Postman Test Cases

### Base URL
```
http://localhost:8008
```

### Headers
Một số endpoints yêu cầu header `X-User-Id` để xác định user hiện tại:
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
```

---

### 📝 Enrollment API Test Cases

#### Test Case 1: Tạo Enrollment - Thành công

**Request**:
- **Method**: `POST`
- **URL**: `http://localhost:8008/api/courses/{courseId}/enroll`
- **Path Variables**:
  - `courseId`: `550e8400-e29b-41d4-a716-446655440000`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (raw JSON):
  ```json
  {
    "courseId": "550e8400-e29b-41d4-a716-446655440000",
    "studentId": "660e8400-e29b-41d4-a716-446655440000",
    "enrolledAt": "2024-01-15T10:00:00",
    "enrollmentStatus": "ACTIVE",
    "paymentStatus": "PENDING"
  }
  ```

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**:
  ```json
  {
    "enrollmentId": "770e8400-e29b-41d4-a716-446655440000",
    "courseId": "550e8400-e29b-41d4-a716-446655440000",
    "studentId": "660e8400-e29b-41d4-a716-446655440000",
    "enrolledAt": "2024-01-15T10:00:00",
    "enrollmentStatus": "ACTIVE",
    "paymentStatus": "PENDING",
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  }
  ```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has enrollmentId", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('enrollmentId');
    pm.expect(jsonData.enrollmentId).to.not.be.null;
});

pm.test("Enrollment status is ACTIVE", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.enrollmentStatus).to.eql("ACTIVE");
});

// Save enrollmentId for later use
var jsonData = pm.response.json();
pm.environment.set("enrollmentId", jsonData.enrollmentId);
```

---

#### Test Case 2: Lấy Enrollment theo ID - Thành công

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8008/api/enrollments/{enrollmentId}`
- **Path Variables**:
  - `enrollmentId`: `{{enrollmentId}}` (sử dụng biến từ test case trước)

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Tương tự như Test Case 1

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response contains enrollment data", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('enrollmentId');
    pm.expect(jsonData).to.have.property('courseId');
    pm.expect(jsonData).to.have.property('studentId');
});
```

---

#### Test Case 3: Lấy Enrollments theo Student ID - Thành công

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8008/api/enrollments/student/{studentId}`
- **Path Variables**:
  - `studentId`: `660e8400-e29b-41d4-a716-446655440000`

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Array of enrollment objects
  ```json
  [
    {
      "enrollmentId": "770e8400-e29b-41d4-a716-446655440000",
      "courseId": "550e8400-e29b-41d4-a716-446655440000",
      "studentId": "660e8400-e29b-41d4-a716-446655440000",
      ...
    }
  ]
  ```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response is an array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
});
```

---

#### Test Case 4: Lấy My Courses (Enrollments của user hiện tại)

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8008/api/enrollments/my-courses`
- **Headers**:
  ```
  X-User-Id: 660e8400-e29b-41d4-a716-446655440000
  ```

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Array of enrollment objects

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response is an array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
});
```

---

#### Test Case 5: Lấy Enrollments theo Course ID - Thành công

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8008/api/courses/{courseId}/enrollments`
- **Path Variables**:
  - `courseId`: `550e8400-e29b-41d4-a716-446655440000`

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Array of enrollment objects

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response is an array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
});
```

---

#### Test Case 6: Lấy Enrollment theo Course ID và Student ID

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8008/api/courses/{courseId}/enrollments/{studentId}`
- **Path Variables**:
  - `courseId`: `550e8400-e29b-41d4-a716-446655440000`
  - `studentId`: `660e8400-e29b-41d4-a716-446655440000`

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Array of enrollment objects

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response is an array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
});
```

---

#### Test Case 7: Cập nhật Enrollment - Thành công

**Request**:
- **Method**: `PUT`
- **URL**: `http://localhost:8008/api/enrollments/{enrollmentId}`
- **Path Variables**:
  - `enrollmentId`: `{{enrollmentId}}`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (raw JSON):
  ```json
  {
    "courseId": "550e8400-e29b-41d4-a716-446655440000",
    "studentId": "660e8400-e29b-41d4-a716-446655440000",
    "enrolledAt": "2024-01-15T10:00:00",
    "enrollmentStatus": "ACTIVE",
    "paymentStatus": "PAID"
  }
  ```

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Updated enrollment object

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Payment status updated to PAID", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.paymentStatus).to.eql("PAID");
});
```

---

#### Test Case 8: Lấy Enrollment - Không tìm thấy (404)

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8008/api/enrollments/00000000-0000-0000-0000-000000000000`

**Expected Response**:
- **Status Code**: `404 Not Found` hoặc `400 Bad Request`

**Postman Test Script**:
```javascript
pm.test("Status code is 4xx", function () {
    pm.expect(pm.response.code).to.be.oneOf([400, 404]);
});
```

---

### 📊 Learning Progress API Test Cases

#### Test Case 9: Tạo Learning Progress - Thành công

**Request**:
- **Method**: `POST`
- **URL**: `http://localhost:8008/api/learning-progress`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (raw JSON):
  ```json
  {
    "enrollmentId": "770e8400-e29b-41d4-a716-446655440000",
    "lessonId": "880e8400-e29b-41d4-a716-446655440000",
    "isCompleted": false
  }
  ```

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**:
  ```json
  {
    "learningProgressId": "990e8400-e29b-41d4-a716-446655440000",
    "enrollmentId": "770e8400-e29b-41d4-a716-446655440000",
    "lessonId": "880e8400-e29b-41d4-a716-446655440000",
    "isCompleted": false,
    "lastAccessedAt": "2024-01-15T10:00:00",
    "completedAt": null
  }
  ```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has learningProgressId", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('learningProgressId');
});

// Save learningProgressId for later use
var jsonData = pm.response.json();
pm.environment.set("learningProgressId", jsonData.learningProgressId);
```

---

#### Test Case 10: Lấy Learning Progress theo ID - Thành công

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8008/api/learning-progress/{learningProgressId}`
- **Path Variables**:
  - `learningProgressId`: `{{learningProgressId}}`

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Learning progress object

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response contains learning progress data", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('learningProgressId');
    pm.expect(jsonData).to.have.property('enrollmentId');
    pm.expect(jsonData).to.have.property('lessonId');
});
```

---

#### Test Case 11: Lấy Learning Progress theo Enrollment ID - Thành công

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8008/api/learning-progress/enrollment/{enrollmentId}`
- **Path Variables**:
  - `enrollmentId`: `{{enrollmentId}}`

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Array of learning progress objects

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response is an array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
});
```

---

#### Test Case 12: Lấy Learning Progress theo Lesson ID và Enrollment ID

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8008/api/learning-progress/lesson/{lessonId}/enrollment/{enrollmentId}`
- **Path Variables**:
  - `lessonId`: `880e8400-e29b-41d4-a716-446655440000`
  - `enrollmentId`: `{{enrollmentId}}`

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Learning progress object

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response contains correct lessonId", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.lessonId).to.eql("880e8400-e29b-41d4-a716-446655440000");
});
```

---

#### Test Case 13: Cập nhật Learning Progress - Thành công

**Request**:
- **Method**: `PUT`
- **URL**: `http://localhost:8008/api/learning-progress/{learningProgressId}`
- **Path Variables**:
  - `learningProgressId`: `{{learningProgressId}}`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (raw JSON):
  ```json
  {
    "enrollmentId": "770e8400-e29b-41d4-a716-446655440000",
    "lessonId": "880e8400-e29b-41d4-a716-446655440000",
    "isCompleted": true
  }
  ```

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Updated learning progress object

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("isCompleted updated to true", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.isCompleted).to.be.true;
});
```

---

#### Test Case 14: Đánh dấu Learning Progress hoàn thành

**Request**:
- **Method**: `POST`
- **URL**: `http://localhost:8008/api/learning-progress/lesson/{lessonId}/enrollment/{enrollmentId}/complete`
- **Path Variables**:
  - `lessonId`: `880e8400-e29b-41d4-a716-446655440000`
  - `enrollmentId`: `{{enrollmentId}}`

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Updated learning progress object với `isCompleted: true`

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("isCompleted is true", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.isCompleted).to.be.true;
});
```

---

### 📈 Course Progress API Test Cases

#### Test Case 15: Tạo Course Progress - Thành công

**Request**:
- **Method**: `POST`
- **URL**: `http://localhost:8008/api/course-progress`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (raw JSON):
  ```json
  {
    "enrollmentId": "770e8400-e29b-41d4-a716-446655440000",
    "lessonsCompleted": 0,
    "totalLessons": 10
  }
  ```

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**:
  ```json
  {
    "courseProgressId": "aa0e8400-e29b-41d4-a716-446655440000",
    "enrollmentId": "770e8400-e29b-41d4-a716-446655440000",
    "overallProgress": 0.0,
    "lessonsCompleted": 0,
    "totalLessons": 10,
    "isCourseCompleted": false,
    "courseCompletedAt": null,
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  }
  ```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has courseProgressId", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('courseProgressId');
});

// Save courseProgressId for later use
var jsonData = pm.response.json();
pm.environment.set("courseProgressId", jsonData.courseProgressId);
```

---

#### Test Case 16: Lấy Course Progress theo ID - Thành công

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8008/api/course-progress/{courseProgressId}`
- **Path Variables**:
  - `courseProgressId`: `{{courseProgressId}}`

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Course progress object

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response contains course progress data", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('courseProgressId');
    pm.expect(jsonData).to.have.property('overallProgress');
    pm.expect(jsonData).to.have.property('lessonsCompleted');
    pm.expect(jsonData).to.have.property('totalLessons');
});
```

---

#### Test Case 17: Lấy Course Progress theo Enrollment ID - Thành công

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8008/api/course-progress/enrollment/{enrollmentId}`
- **Path Variables**:
  - `enrollmentId`: `{{enrollmentId}}`

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Course progress object

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response contains correct enrollmentId", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.enrollmentId).to.eql(pm.environment.get("enrollmentId"));
});
```

---

#### Test Case 18: Cập nhật Course Progress - Thành công

**Request**:
- **Method**: `PUT`
- **URL**: `http://localhost:8008/api/course-progress/{courseProgressId}`
- **Path Variables**:
  - `courseProgressId`: `{{courseProgressId}}`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (raw JSON):
  ```json
  {
    "enrollmentId": "770e8400-e29b-41d4-a716-446655440000",
    "lessonsCompleted": 5,
    "totalLessons": 10
  }
  ```

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**: Updated course progress object với `overallProgress: 50.0`

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Lessons completed updated", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.lessonsCompleted).to.eql(5);
});

pm.test("Overall progress calculated correctly", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.overallProgress).to.eql(50.0);
});
```

---

### 🔍 Error Handling Test Cases

#### Test Case 19: Tạo Enrollment - Thiếu thông tin bắt buộc

**Request**:
- **Method**: `POST`
- **URL**: `http://localhost:8008/api/courses/{courseId}/enroll`
- **Body** (raw JSON):
  ```json
  {
    "studentId": "660e8400-e29b-41d4-a716-446655440000"
  }
  ```

**Expected Response**:
- **Status Code**: `400 Bad Request` hoặc `500 Internal Server Error`

**Postman Test Script**:
```javascript
pm.test("Status code is 4xx or 5xx", function () {
    pm.expect(pm.response.code).to.be.oneOf([400, 500]);
});
```

---

#### Test Case 20: Lấy Learning Progress - Không tìm thấy (404)

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8008/api/learning-progress/00000000-0000-0000-0000-000000000000`

**Expected Response**:
- **Status Code**: `404 Not Found` hoặc `400 Bad Request`

**Postman Test Script**:
```javascript
pm.test("Status code is 4xx", function () {
    pm.expect(pm.response.code).to.be.oneOf([400, 404]);
});
```

---

### 📦 Postman Collection JSON

Bạn có thể import collection sau vào Postman:

```json
{
  "info": {
    "name": "Enrollment Service API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8008",
      "type": "string"
    },
    {
      "key": "enrollmentId",
      "value": "",
      "type": "string"
    },
    {
      "key": "learningProgressId",
      "value": "",
      "type": "string"
    },
    {
      "key": "courseProgressId",
      "value": "",
      "type": "string"
    }
  ],
  "item": [
    {
      "name": "Enrollment",
      "item": [
        {
          "name": "Create Enrollment",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"courseId\": \"550e8400-e29b-41d4-a716-446655440000\",\n  \"studentId\": \"660e8400-e29b-41d4-a716-446655440000\",\n  \"enrolledAt\": \"2024-01-15T10:00:00\",\n  \"enrollmentStatus\": \"ACTIVE\",\n  \"paymentStatus\": \"PENDING\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/courses/550e8400-e29b-41d4-a716-446655440000/enroll",
              "host": ["{{baseUrl}}"],
              "path": ["api", "courses", "550e8400-e29b-41d4-a716-446655440000", "enroll"]
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test(\"Status code is 200\", function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "",
                  "var jsonData = pm.response.json();",
                  "pm.collectionVariables.set(\"enrollmentId\", jsonData.enrollmentId);"
                ]
              }
            }
          ]
        },
        {
          "name": "Get Enrollment by ID",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/api/enrollments/{{enrollmentId}}",
              "host": ["{{baseUrl}}"],
              "path": ["api", "enrollments", "{{enrollmentId}}"]
            }
          }
        },
        {
          "name": "Get My Courses",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "X-User-Id",
                "value": "660e8400-e29b-41d4-a716-446655440000"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/enrollments/my-courses",
              "host": ["{{baseUrl}}"],
              "path": ["api", "enrollments", "my-courses"]
            }
          }
        },
        {
          "name": "Update Enrollment",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"courseId\": \"550e8400-e29b-41d4-a716-446655440000\",\n  \"studentId\": \"660e8400-e29b-41d4-a716-446655440000\",\n  \"enrolledAt\": \"2024-01-15T10:00:00\",\n  \"enrollmentStatus\": \"ACTIVE\",\n  \"paymentStatus\": \"PAID\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/enrollments/{{enrollmentId}}",
              "host": ["{{baseUrl}}"],
              "path": ["api", "enrollments", "{{enrollmentId}}"]
            }
          }
        }
      ]
    },
    {
      "name": "Learning Progress",
      "item": [
        {
          "name": "Create Learning Progress",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"enrollmentId\": \"{{enrollmentId}}\",\n  \"lessonId\": \"880e8400-e29b-41d4-a716-446655440000\",\n  \"isCompleted\": false\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/learning-progress",
              "host": ["{{baseUrl}}"],
              "path": ["api", "learning-progress"]
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "var jsonData = pm.response.json();",
                  "pm.collectionVariables.set(\"learningProgressId\", jsonData.learningProgressId);"
                ]
              }
            }
          ]
        },
        {
          "name": "Get Learning Progress by ID",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/api/learning-progress/{{learningProgressId}}",
              "host": ["{{baseUrl}}"],
              "path": ["api", "learning-progress", "{{learningProgressId}}"]
            }
          }
        },
        {
          "name": "Get Learning Progress by Enrollment",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/api/learning-progress/enrollment/{{enrollmentId}}",
              "host": ["{{baseUrl}}"],
              "path": ["api", "learning-progress", "enrollment", "{{enrollmentId}}"]
            }
          }
        }
      ]
    },
    {
      "name": "Course Progress",
      "item": [
        {
          "name": "Create Course Progress",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"enrollmentId\": \"{{enrollmentId}}\",\n  \"lessonsCompleted\": 0,\n  \"totalLessons\": 10\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/course-progress",
              "host": ["{{baseUrl}}"],
              "path": ["api", "course-progress"]
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "var jsonData = pm.response.json();",
                  "pm.collectionVariables.set(\"courseProgressId\", jsonData.courseProgressId);"
                ]
              }
            }
          ]
        },
        {
          "name": "Get Course Progress by ID",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/api/course-progress/{{courseProgressId}}",
              "host": ["{{baseUrl}}"],
              "path": ["api", "course-progress", "{{courseProgressId}}"]
            }
          }
        },
        {
          "name": "Get Course Progress by Enrollment",
          "request": {
            "method": "GET",
            "url": {
              "raw": "{{baseUrl}}/api/course-progress/enrollment/{{enrollmentId}}",
              "host": ["{{baseUrl}}"],
              "path": ["api", "course-progress", "enrollment", "{{enrollmentId}}"]
            }
          }
        }
      ]
    }
  ]
}
```

---

### 📝 Lưu ý khi test

1. **Environment Variables**: Sử dụng Postman Environment để lưu các biến như `enrollmentId`, `learningProgressId`, `courseProgressId` để sử dụng trong các test case tiếp theo.

2. **UUID Format**: Tất cả các UUID phải đúng format: `550e8400-e29b-41d4-a716-446655440000`

3. **Enrollment Status Values**: 
   - `ACTIVE`
   - `COMPLETED`
   - `SUSPENDED`
   - `CANCELLED`

4. **Payment Status Values**:
   - `PENDING`
   - `PAID`
   - `REFUNDED`
   - `CANCELLED`

5. **Date Format**: Sử dụng ISO 8601 format: `2024-01-15T10:00:00`

6. **Service Port**: Mặc định service chạy trên port `8008`, có thể thay đổi qua environment variable `ENROLLMENT_SERVICE_SERVER_PORT`