# Project structure
The project currently includes several modules (services):
1. API Gateway
2. Auth service
3. User service
4. Course service
5. Content service
6. Enrollment service
7. Payment service
8. Analytics service

The description of each service will be described bellow.

## API Gateway
It will be an entry point of all request. 

**Base URL: `http://localhost:8080`**

## Auth service

Responsible for authentication.  

***Note: If user fist time login (API response has `firstLogin` field is true, so frontend will force user to update profile).***

**API sample:**
1. Signup
- Method: POST
- URL: `{{baseUrl}}/api/auth/signup`
- Body (raw JSON):
```json
{
  "email": "example@gmail.com",
  "password": "12345",
  "passwordConfirm": "12345"
}
```
- Response (String):
```
Signup successful for user: example@gmail.com
```

2. Login
- Method: POST
- URL: `{{baseUrl}}/api/auth/login`
- Body (raw JSON):
```json
{
    "email": "example@gmail.com",
    "password": "12345"
}
```
- Response (raw JSON):
```json
{
    "userId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlQGdtYWlsLmNvbSIsInJvbGVzIjpbIlNUVURFTlQiXSwiaWF0IjoxNzYwMTQ5NDcwLCJleHAiOjE3NjAyMzU4NzB9.L9Rg4dmOtzWtL9EbXwF6zB6GiW2yc1uLfaOW91UzJLo",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlQGdtYWlsLmNvbSIsImlhdCI6MTc2MDE0OTQ3MCwiZXhwIjoxNzYwNTgxNDcwfQ.um6ljpblLhK8IPxFY8uDpYt__GUS7hhNmE6Ie5rKwsw",
    "tokenType": "Bearer",
    "email": "example@gmail.com",
    "role": "STUDENT",
    "firstLogin": true
}
```
*Note: role is string array (currently not necessary)*
3. Refresh
- Method: POST
- URL: `{{baseUrl}}/api/auth/refresh`
- Body (raw JSON):
```json
{
  "accessToken": "{accessToken}",
  "refreshToken": "{refreshToken}"
}
```
- Response (raw JSON):
```json
{
"newAccessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlQGdtYWlsLmNvbSIsInJvbGVzIjpbIlNUVURFTlQiXSwiaWF0IjoxNzYwMTQ5NjIwLCJleHAiOjE3NjAyMzYwMjB9.2MOMEcC7MGUNCBFB54cD6KdJuaVm4J3gVQBrHnZEhQI",
"refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlQGdtYWlsLmNvbSIsImlhdCI6MTc2MDE0OTQ3MCwiZXhwIjoxNzYwNTgxNDcwfQ.um6ljpblLhK8IPxFY8uDpYt__GUS7hhNmE6Ie5rKwsw"
}
```

4) Logout
- Method: POST
- URL: `{{baseUrl}}/api/auth/logout`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response (String):
```
Logged out successfully
```

### Admin Login và Khởi tạo Analytics

**Sau khi admin đăng nhập thành công, nên khởi tạo dữ liệu analytics ngay:**

1. Login Admin
- Method: POST
- URL: `{{baseUrl}}/api/auth/login`
- Body (raw JSON):
```json
{
    "email": "email",
    "password": "password"
}
```
- Response (raw JSON):
```json
{
    "userId": "admin-user-id-here",
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "email": "",
    "role": "",
    "firstLogin": false
}
```

2. Khởi tạo tổng quan platform cho kỳ hiện tại (sau khi login admin)
- Method: POST
- URL: `{{baseUrl}}/api/analytics/admin/platform/overview/initialize?period=DAILY`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: `period` (DAILY, WEEKLY, MONTHLY, YEARLY)
- Response (raw JSON):
```json
{
  "overviewId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "totalUsers": 0,
  "totalCourses": 0,
  "totalEnrollments": 0,
  "totalRevenue": 0.00,
  "averageCompletionRate": 0.0,
  "startDate": "2025-12-13",
  "endDate": "2025-12-13",
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

3. (Tùy chọn) Generate tổng quan platform cho khoảng thời gian cụ thể
- Method: POST
- URL: `{{baseUrl}}/api/analytics/admin/platform/overview/generate?period=DAILY&startDate=2025-12-01&endDate=2025-12-13`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: 
  - `period` (DAILY, WEEKLY, MONTHLY, YEARLY)
  - `startDate` (format: YYYY-MM-DD)
  - `endDate` (format: YYYY-MM-DD)
- Response: (same as above)

***Note: Sau khi admin login lần đầu hoặc khởi tạo môi trường mới, nên gọi endpoint initialize để có bản ghi overview khởi điểm. Scheduler sẽ tự động chạy định kỳ sau đó.***

## User service

Responsible for managing the user information.

1. Lấy hồ sơ theo email hiện tại
- Method: GET
- URL: `{{baseUrl}}/api/users/profiles/me`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response:
```json
{
  "userId": "9f5f26c0-7a7f-483a-8678-c63dbe38a367",
    "userSlug": "default-user",
    "fullName": "Nguyen Van A",
    "avatarUrl": "https://example.com/a.jpg",
    "bio": "Hello there",
    "phoneNumber": "+84901234567",
    "address": "Hanoi, Vietnam",
    "createdAt": "2025-12-13T13:46:52.048922",
    "updatedAt": "2025-12-13T13:46:52.048937"
}
```

2. Lấy hồ sơ theo `userId`
- Method: GET
- URL: `{{baseUrl}}/api/users/profiles/id/{userId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response:
```json
{
  "userId": "9f5f26c0-7a7f-483a-8678-c63dbe38a367",
  "userSlug": "default-user",
  "fullName": "Nguyen Van A",
  "avatarUrl": "https://example.com/a.jpg",
  "bio": "Hello there",
  "phoneNumber": "+84901234567",
  "address": "Hanoi, Vietnam",
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

3. Cập nhật hồ sơ theo `userId`
- Method: PUT
- URL: `{{baseUrl}}/api/users/profiles/id/{userId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Body (raw JSON):
```json
{
  "userId": "9f5f26c0-7a7f-483a-8678-c63dbe38a367",
  "fullName": "Nguyen Van B",
  "avatarUrl": "https://example.com/a.jpg",
  "bio": "Hello there",
  "phoneNumber": "+84901234567",
  "address": "Hanoi, Vietnam",
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```
- Response:
```json
{
  "userId": "9f5f26c0-7a7f-483a-8678-c63dbe38a367",
  "userSlug": "default-user",
  "fullName": "Nguyen Van B",
  "avatarUrl": "https://example.com/a.jpg",
  "bio": "Hello there",
  "phoneNumber": "+84901234567",
  "address": "Hanoi, Vietnam",
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

## Course service

Responsible for managing courses, sections, lessons, content metadata, and categories.

**API sample:**

### Courses

1. Tạo khóa học
- Method: POST
- URL: `{{baseUrl}}/api/courses`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Body (raw JSON):
```json
{
  "title": "Introduction to Java Programming",
  "description": "Learn Java from scratch",
  "thumbnailUrl": "https://example.com/thumbnail.jpg",
  "price": 99.99,
  "level": "BEGINNER",
  "categoryName": "Programming"
}
```
- Response (raw JSON):
```json
{
  "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "courseSlug": "introduction-to-java-programming",
  "title": "Introduction to Java Programming",
  "description": "Learn Java from scratch",
  "thumbnailUrl": "https://example.com/thumbnail.jpg",
  "price": 99.99,
  "level": "BEGINNER",
  "categoryName": "Programming",
  "instructorId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

2. Lấy khóa học theo ID
- Method: GET
- URL: `{{baseUrl}}/api/courses/id/{courseId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response:
```json
{
  "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "courseSlug": "introduction-to-java-programming",
  "title": "Introduction to Java Programming",
  "description": "Learn Java from scratch",
  "thumbnailUrl": "https://example.com/thumbnail.jpg",
  "price": 99.99,
  "level": "BEGINNER",
  "categoryName": "Programming",
  "instructorId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

3. Lấy khóa học theo slug
- Method: GET
- URL: `{{baseUrl}}/api/courses/slug/{courseSlug}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response:
```json
{
  "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "courseSlug": "introduction-to-java-programming",
  "title": "Introduction to Java Programming",
  "description": "Learn Java from scratch",
  "thumbnailUrl": "https://example.com/thumbnail.jpg",
  "price": 99.99,
  "level": "BEGINNER",
  "categoryName": "Programming",
  "instructorId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

4. Cập nhật khóa học theo ID
- Method: PUT
- URL: `{{baseUrl}}/api/courses/id/{courseId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Body (raw JSON):
```json
{
  "title": "Advanced Java Programming",
  "description": "Advanced Java concepts",
  "thumbnailUrl": "https://example.com/thumbnail2.jpg",
  "price": 149.99,
  "level": "INTERMEDIATE",
  "categoryName": "Programming"
}
```
- Response:
```json
{
  "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "courseSlug": "introduction-to-java-programming",
  "title": "Advanced Java Programming",
  "description": "Advanced Java concepts",
  "thumbnailUrl": "https://example.com/thumbnail2.jpg",
  "price": 149.99,
  "level": "INTERMEDIATE",
  "categoryName": "Programming",
  "instructorId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

5. Cập nhật khóa học theo slug
- Method: PUT
- URL: `{{baseUrl}}/api/courses/slug/{courseSlug}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Body: (same as above)
- Response: (same as above)

6. Lấy tất cả khóa học (có phân trang)
- Method: GET
- URL: `{{baseUrl}}/api/courses?page=0&size=10&sort=createdAt,desc`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response (raw JSON):
```json
{
  "content": [
    {
      "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "courseSlug": "introduction-to-java-programming",
      "title": "Introduction to Java Programming",
      "description": "Learn Java from scratch",
      "thumbnailUrl": "https://example.com/thumbnail.jpg",
      "price": 99.99,
      "level": "BEGINNER",
      "categoryName": "Programming",
      "instructorId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
      "createdAt": "2025-12-13T13:46:52.048922",
      "updatedAt": "2025-12-13T13:46:52.048937"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

7. Lấy khóa học theo danh mục
- Method: GET
- URL: `{{baseUrl}}/api/courses/category/{categoryName}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response (raw JSON):
```json
[
  {
    "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "courseSlug": "introduction-to-java-programming",
    "title": "Introduction to Java Programming",
    "description": "Learn Java from scratch",
    "thumbnailUrl": "https://example.com/thumbnail.jpg",
    "price": 99.99,
    "level": "BEGINNER",
    "categoryName": "Programming",
    "instructorId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
    "createdAt": "2025-12-13T13:46:52.048922",
    "updatedAt": "2025-12-13T13:46:52.048937"
  }
]
```

8. Lấy tổng số bài học của khóa học
- Method: GET
- URL: `{{baseUrl}}/api/courses/{courseId}/total-lessons`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response (raw JSON):
```json
10
```

### Sections

1. Tạo section cho khóa học
- Method: POST
- URL: `{{baseUrl}}/api/courses/id/{courseId}/sections`
- Headers: `Authorization: Bearer {{accessToken}}`
- Body (raw JSON):
```json
{
  "title": "Chapter 1: Basics",
  "description": "Introduction to basics",
  "orderIndex": 1
}
```
- Response (raw JSON):
```json
{
  "sectionId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "sectionSlug": "chapter-1-basics",
  "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "title": "Chapter 1: Basics",
  "description": "Introduction to basics",
  "orderIndex": 1,
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

2. Lấy section theo ID
- Method: GET
- URL: `{{baseUrl}}/api/courses/sections/id/{sectionId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

3. Lấy section theo slug
- Method: GET
- URL: `{{baseUrl}}/api/courses/sections/slug/{sectionSlug}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

4. Cập nhật section theo ID
- Method: PUT
- URL: `{{baseUrl}}/api/courses/id/{courseId}/sections/id/{sectionId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Body (raw JSON):
```json
{
  "title": "Chapter 1: Advanced Basics",
  "description": "Advanced introduction",
  "orderIndex": 1
}
```
- Response: (same as above)

5. Lấy tất cả sections của khóa học (theo ID)
- Method: GET
- URL: `{{baseUrl}}/api/courses/id/{courseId}/sections`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response (raw JSON):
```json
[
  {
    "sectionId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "sectionSlug": "chapter-1-basics",
    "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "title": "Chapter 1: Basics",
    "description": "Introduction to basics",
    "orderIndex": 1,
    "createdAt": "2025-12-13T13:46:52.048922",
    "updatedAt": "2025-12-13T13:46:52.048937"
  }
]
```

6. Lấy tất cả sections của khóa học (theo slug)
- Method: GET
- URL: `{{baseUrl}}/api/courses/slug/{courseSlug}/sections`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

### Lessons

1. Tạo lesson cho section
- Method: POST
- URL: `{{baseUrl}}/api/courses/id/{courseId}/sections/id/{sectionId}/lessons`
- Headers: `Authorization: Bearer {{accessToken}}`
- Body (raw JSON):
```json
{
  "title": "Lesson 1: Variables",
  "orderIndex": 1
}
```
- Response (raw JSON):
```json
{
  "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "lessonSlug": "lesson-1-variables",
  "title": "Lesson 1: Variables",
  "sectionId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "orderIndex": 1,
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

2. Lấy lesson theo ID
- Method: GET
- URL: `{{baseUrl}}/api/courses/lessons/id/{lessonId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

3. Lấy lesson theo slug
- Method: GET
- URL: `{{baseUrl}}/api/courses/lessons/slug/{lessonSlug}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

4. Cập nhật lesson theo ID
- Method: PUT
- URL: `{{baseUrl}}/api/courses/id/{courseId}/sections/id/{sectionId}/lessons/id/{lessonId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Body (raw JSON):
```json
{
  "title": "Lesson 1: Variables and Data Types",
  "orderIndex": 1
}
```
- Response: (same as above)

5. Lấy tất cả lessons của section (theo ID)
- Method: GET
- URL: `{{baseUrl}}/api/courses/sections/id/{sectionId}/lessons`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response (raw JSON):
```json
[
  {
    "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "lessonSlug": "lesson-1-variables",
    "title": "Lesson 1: Variables",
    "sectionId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "orderIndex": 1,
    "createdAt": "2025-12-13T13:46:52.048922",
    "updatedAt": "2025-12-13T13:46:52.048937"
  }
]
```

6. Lấy tất cả lessons của section (theo slug)
- Method: GET
- URL: `{{baseUrl}}/api/courses/sections/slug/{sectionSlug}/lessons`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

### Content Metadata

1. Tạo content metadata cho lesson
- Method: POST
- URL: `{{baseUrl}}/api/courses/id/{courseId}/sections/id/{sectionId}/lessons/id/{lessonId}/contents`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Path params: `courseId`, `sectionId`, `lessonId`
- Body (raw JSON):
```json
{
  "contentUrl": "https://example.com/video.mp4",
  "orderIndex": 1
}
```
- Response (raw JSON):
```json
{
  "contentId": "d4e5f6a7-b8c9-0123-def4-234567890123",
  "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "contentUrl": "https://example.com/video.mp4",
  "orderIndex": 1,
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

2. Lấy tất cả content metadata của lesson
- Method: GET
- URL: `{{baseUrl}}/api/courses/lessons/id/{lessonId}/contents`
- Headers: `Authorization: Bearer {{accessToken}}`
- Path params: `lessonId`
- Response (raw JSON):
```json
[
  {
    "contentId": "d4e5f6a7-b8c9-0123-def4-234567890123",
    "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "contentUrl": "https://example.com/video.mp4",
    "orderIndex": 1,
    "createdAt": "2025-12-13T13:46:52.048922",
    "updatedAt": "2025-12-13T13:46:52.048937"
  }
]
```

3. Lấy content metadata theo ID
- Method: GET
- URL: `{{baseUrl}}/api/courses/content/id/{contentId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Path params: `contentId`
- Response: (same as above, single object)

4. Cập nhật content metadata
- Method: PUT
- URL: `{{baseUrl}}/api/courses/id/{courseId}/sections/id/{sectionId}/lessons/id/{lessonId}/contents/id/{contentId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Path params: `courseId`, `sectionId`, `lessonId`, `contentId`
- Body (raw JSON):
```json
{
  "contentUrl": "https://example.com/video2.mp4",
  "orderIndex": 1
}
```
- Response: (same as above)

### Categories

1. Tạo category
- Method: POST
- URL: `{{baseUrl}}/api/categories`
- Headers: `Authorization: Bearer {{accessToken}}`
- Body (raw JSON):
```json
{
  "categoryName": "Web Development",
  "description": "Courses about web development"
}
```
- Response (raw JSON):
```json
{
  "categoryName": "Web Development",
  "description": "Courses about web development",
  "isPredefined": false
}
```

2. Lấy category theo tên
- Method: GET
- URL: `{{baseUrl}}/api/categories/{categoryName}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

3. Cập nhật category
- Method: PUT
- URL: `{{baseUrl}}/api/categories/{categoryName}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Body (raw JSON):
```json
{
  "categoryName": "Web Development",
  "description": "Updated description for web development courses"
}
```
- Response: (same as above)

4. Lấy tất cả categories
- Method: GET
- URL: `{{baseUrl}}/api/categories`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response (raw JSON):
```json
[
  {
    "categoryName": "Programming",
    "description": "Programming courses",
    "isPredefined": true
  },
  {
    "categoryName": "Web Development",
    "description": "Courses about web development",
    "isPredefined": false
  }
]
```

5. Lấy predefined categories
- Method: GET
- URL: `{{baseUrl}}/api/categories/predefined`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above, filtered by isPredefined = true)

6. Lấy custom categories
- Method: GET
- URL: `{{baseUrl}}/api/categories/custom`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above, filtered by isPredefined = false)

## Payment service

Responsible for creating VNPay payment URLs and handling VNPay IPN callbacks to confirm payments and publish payment events.

**API sample:**

1. Tạo payment (lấy VNPay URL)
- Method: POST
- URL: `{{baseUrl}}/api/payment`
- Body (raw JSON):
```json
{
  "amount": "199000",
  "userId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
  "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "instructorId": "40b3dd3f-8e39-4ed9-ad60-f26244a15a11",
  "courseSlug": "introduction-to-java-programming"
}
```
- Response (raw JSON):
```json
{
  "url": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?..."
}
```

2. VNPay IPN callback (xử lý kết quả thanh toán)
- Method: GET
- URL: `{{baseUrl}}/api/payment/ipn?{VNPayParams}`
- Query params (ví dụ):
```text
vnp_Amount=19900000
vnp_ResponseCode=00
vnp_TransactionStatus=00
vnp_TxnRef=12345678
userId=30a2cc2f-7d29-4cd9-bd60-f26244a15a78
courseId=a1b2c3d4-e5f6-7890-abcd-ef1234567890
instructorId=40b3dd3f-8e39-4ed9-ad60-f26244a15a11
courseSlug=introduction-to-java-programming
amount=199000
vnp_SecureHash={generatedByVNPay}
```
- Response (raw JSON):
```json
{
  "RspCode": "00",
  "Message": "Payment confirmed successfully"
}
```

## Enrollment service

Responsible for managing student enrollments, course progress, and learning progress.

**API sample:**

### Enrollments

1. Đăng ký khóa học
- Method: POST
- URL: `{{baseUrl}}/api/admin/enrollments`
- Headers: `Authorization: Bearer {{accessToken}}`
- Body (raw JSON):
```json
{
  "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "courseSlug": "introduction-to-java-programming",
  "studentId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
  "enrollmentStatus": "ACTIVE",
  "paymentStatus": "PAID"
}
```
- Response (raw JSON):
```json
{
  "enrollmentId": "e5f6a7b8-c9d0-1234-ef56-789012345678",
  "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "courseSlug": "introduction-to-java-programming",
  "studentId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
  "enrolledAt": "2025-12-13T13:46:52.048922",
  "enrollmentStatus": "ACTIVE",
  "paymentStatus": "PAID",
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

2. Lấy enrollment theo ID
- Method: GET
- URL: `{{baseUrl}}/api/enrollments/id/{enrollmentId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Response: (same as above)

3. Lấy enrollments theo course ID
- Method: GET
- URL: `{{baseUrl}}/api/courses/id/{courseId}/enrollments`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

4. Lấy enrollments theo course ID và student ID
- Method: GET
- URL: `{{baseUrl}}/api/courses/id/{courseId}/enrollments/student/id/{studentId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

5. Lấy các khóa học của tôi
- Method: GET
- URL: `{{baseUrl}}/api/enrollments/my-courses`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Response: (same as above)

6. Cập nhật enrollment
- Method: PUT
- URL: `{{baseUrl}}/api/enrollments/id/{enrollmentId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Body (raw JSON):
```json
{
  "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "courseSlug": "introduction-to-java-programming",
  "studentId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
  "enrollmentStatus": "ACTIVE",
  "paymentStatus": "PAID"
}
```
- Response: (same as above)

### Course Progress

1. Lấy course progress theo ID
- Method: GET
- URL: `{{baseUrl}}/api/course-progress/id/{courseProgressId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response (raw JSON):
```json
{
  "courseProgressId": "f6a7b8c9-d0e1-2345-f678-901234567890",
  "enrollmentId": "e5f6a7b8-c9d0-1234-ef56-789012345678",
  "overallProgress": 50.0,
  "lessonsCompleted": 5,
  "totalLessons": 10,
  "isCourseCompleted": false,
  "courseCompletedAt": null,
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

2. Lấy course progress theo enrollment ID
- Method: GET
- URL: `{{baseUrl}}/api/course-progress/enrollment/id/{enrollmentId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

### Learning Progress

1. Lấy learning progress theo ID
- Method: GET
- URL: `{{baseUrl}}/api/learning-progress/id/{learningProgressId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Response (raw JSON):
```json
{
  "learningProgressId": "a7b8c9d0-e1f2-3456-789a-bcdef0123456",
  "enrollmentId": "e5f6a7b8-c9d0-1234-ef56-789012345678",
  "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "isCompleted": false,
  "lastAccessedAt": "2025-12-13T13:46:52.048922",
  "completedAt": null
}
```

2. Lấy learning progress theo enrollment ID
- Method: GET
- URL: `{{baseUrl}}/api/learning-progress/enrollment/id/{enrollmentId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Response (raw JSON):
```json
[
  {
    "learningProgressId": "a7b8c9d0-e1f2-3456-789a-bcdef0123456",
    "enrollmentId": "e5f6a7b8-c9d0-1234-ef56-789012345678",
    "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "isCompleted": true,
    "lastAccessedAt": "2025-12-13T14:00:00.000000",
    "completedAt": "2025-12-13T14:00:00.000000"
  }
]
```

3. Lấy learning progress theo lesson ID và enrollment ID
- Method: GET
- URL: `{{baseUrl}}/api/learning-progress/lesson/id/{lessonId}/enrollment/id/{enrollmentId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Response: (same as above, single object)

4. Đánh dấu lesson hoàn thành
- Method: POST
- URL: `{{baseUrl}}/api/learning-progress/lesson/id/{lessonId}/enrollment/id/{enrollmentId}/complete`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Response (raw JSON):
```json
{
  "learningProgressId": "a7b8c9d0-e1f2-3456-789a-bcdef0123456",
  "enrollmentId": "e5f6a7b8-c9d0-1234-ef56-789012345678",
  "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "isCompleted": true,
  "lastAccessedAt": "2025-12-13T14:00:00.000000",
  "completedAt": "2025-12-13T14:00:00.000000"
}
```

## Analytics service

Responsible for tracking and analyzing platform metrics, instructor statistics, revenue analytics, and user growth metrics.

**API sample:**

### Instructor Analytics

1. Lấy tổng quan thống kê của instructor
- Method: GET
- URL: `{{baseUrl}}/api/analytics/instructor/overview`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {instructorId}`
- Response (raw JSON):
```json
{
  "instructorId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
  "totalCourses": 5,
  "totalStudents": 120,
  "totalRevenue": 15000000.00,
  "averageCompletionRate": 75.5,
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T14:00:00.000000"
}
```

2. Lấy thống kê tất cả khóa học của instructor
- Method: GET
- URL: `{{baseUrl}}/api/analytics/instructor/courses`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {instructorId}`
- Response (raw JSON):
```json
[
  {
    "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "totalStudents": 50,
    "totalRevenue": 5000000.00,
    "completionRate": 80.0,
    "createdAt": "2025-12-13T13:46:52.048922",
    "updatedAt": "2025-12-13T14:00:00.000000"
  }
]
```

3. Lấy thống kê của một khóa học cụ thể
- Method: GET
- URL: `{{baseUrl}}/api/analytics/instructor/courses/{courseId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {instructorId}`
- Response: (same as above, single object)

4. Lấy top khóa học theo doanh thu
- Method: GET
- URL: `{{baseUrl}}/api/analytics/instructor/courses/top-revenue`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {instructorId}`
- Response: (same as above)

5. Lấy top khóa học theo tỉ lệ hoàn thành
- Method: GET
- URL: `{{baseUrl}}/api/analytics/instructor/courses/top-completion`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {instructorId}`
- Response: (same as above)

6. Lấy thống kê theo ngày
- Method: GET
- URL: `{{baseUrl}}/api/analytics/instructor/daily?startDate=2025-12-01&endDate=2025-12-31`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {instructorId}`
- Query params (optional): `startDate`, `endDate` (format: YYYY-MM-DD)
- Response (raw JSON):
```json
[
  {
    "date": "2025-12-13",
    "newEnrollments": 5,
    "activeStudents": 10,
    "dailyRevenue": 500000.00,
    "courseCompletions": 2,
    "createdAt": "2025-12-13T13:46:52.048922",
    "updatedAt": "2025-12-13T14:00:00.000000"
  }
]
```

7. Lấy thống kê theo ngày cụ thể
- Method: GET
- URL: `{{baseUrl}}/api/analytics/instructor/daily/{date}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {instructorId}`
- Path param: `date` (format: YYYY-MM-DD)
- Response: (same as above, single object)

8. Tính lại tỉ lệ hoàn thành trung bình
- Method: POST
- URL: `{{baseUrl}}/api/analytics/instructor/overview/recalculate-completion-rate`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {instructorId}`
- Response: Status 202 Accepted

### Admin Analytics - Instructor

1. Lấy thống kê của một instructor
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/instructors/{instructorId}/stats`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response (raw JSON):
```json
{
  "instructorId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
  "totalCourses": 5,
  "totalStudents": 120,
  "averageCompletionRate": 75.5
}
```

2. Lấy top instructors theo số học viên
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/instructors/top/students?limit=10`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: `limit` (default: 10)
- Response (raw JSON):
```json
[
  {
    "instructorId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
    "totalCourses": 5,
    "totalStudents": 120,
    "averageCompletionRate": 75.5
  }
]
```

3. Lấy top instructors theo số khóa học
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/instructors/top/courses?limit=10`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: `limit` (default: 10)
- Response: (same as above)

4. Lấy top instructors theo doanh thu
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/instructors/top/revenue?period=MONTHLY&endDate=2025-12-31&limit=10`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: 
  - `period` (DAILY, WEEKLY, MONTHLY, YEARLY)
  - `endDate` (format: YYYY-MM-DD)
  - `limit` (default: 10)
- Response (raw JSON):
```json
[
  {
    "instructorRevenueId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "instructorId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
    "period": "MONTHLY",
    "startDate": "2025-12-01",
    "endDate": "2025-12-31",
    "totalRevenue": 5000000.00,
    "totalEnrollments": 50,
    "totalCourses": 5,
    "topPerformingCourses": [
      {
        "courseId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "courseTitle": "Introduction to Java Programming",
        "enrollmentCount": 20,
        "revenue": 2000000.00
      }
    ],
    "createdAt": "2025-12-13T13:46:52.048922",
    "updatedAt": "2025-12-13T14:00:00.000000"
  }
]
```

5. Lấy doanh thu của instructor trong một khoảng thời gian
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/instructors/{instructorId}/revenue?period=MONTHLY&startDate=2025-12-01&endDate=2025-12-31`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: 
  - `period` (DAILY, WEEKLY, MONTHLY, YEARLY)
  - `startDate` (format: YYYY-MM-DD)
  - `endDate` (format: YYYY-MM-DD)
- Response: (same as above, single object)

6. Lấy lịch sử doanh thu của instructor
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/instructors/{instructorId}/revenue/history?period=MONTHLY`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: `period` (DAILY, WEEKLY, MONTHLY, YEARLY)
- Response: (same as above, array)

### Admin Analytics - Platform

1. Lấy tổng quan platform mới nhất
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/platform/overview/latest?period=MONTHLY`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: `period` (DAILY, WEEKLY, MONTHLY, YEARLY)
- Response (raw JSON):
```json
{
  "overviewId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "totalUsers": 1000,
  "totalCourses": 50,
  "totalEnrollments": 500,
  "totalRevenue": 50000000.00,
  "averageCompletionRate": 70.5,
  "startDate": "2025-12-01",
  "endDate": "2025-12-31",
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T14:00:00.000000"
}
```

2. Lấy tổng quan platform cho một khoảng thời gian
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/platform/overview?period=MONTHLY&startDate=2025-12-01&endDate=2025-12-31`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: 
  - `period` (DAILY, WEEKLY, MONTHLY, YEARLY)
  - `startDate` (format: YYYY-MM-DD)
  - `endDate` (format: YYYY-MM-DD)
- Response: (same as above)

3. Lấy lịch sử tổng quan platform
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/platform/overview/history?period=MONTHLY&limit=10`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: 
  - `period` (DAILY, WEEKLY, MONTHLY, YEARLY)
  - `limit` (default: 10)
- Response: (same as above, array)

4. Tạo tổng quan platform mới
- Method: POST
- URL: `{{baseUrl}}/api/analytics/admin/platform/overview/generate?period=MONTHLY&startDate=2025-12-01&endDate=2025-12-31`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: 
  - `period` (DAILY, WEEKLY, MONTHLY, YEARLY)
  - `startDate` (format: YYYY-MM-DD)
  - `endDate` (format: YYYY-MM-DD)
- Response: (same as above)

5. Khởi tạo tổng quan cho kỳ hiện tại
- Method: POST
- URL: `{{baseUrl}}/api/analytics/admin/platform/overview/initialize?period=MONTHLY`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: `period` (DAILY, WEEKLY, MONTHLY, YEARLY)
- Response: (same as above)

### Admin Analytics - Revenue

1. Lấy doanh thu theo ngày
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/revenue/daily/{date}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Path param: `date` (format: YYYY-MM-DD)
- Response (raw JSON):
```json
{
  "date": "2025-12-13",
  "totalRevenue": 5000000.00,
  "totalTransactions": 50
}
```

2. Lấy lịch sử doanh thu
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/revenue/history?startDate=2025-12-01&endDate=2025-12-31`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: 
  - `startDate` (format: YYYY-MM-DD)
  - `endDate` (format: YYYY-MM-DD)
- Response (raw JSON):
```json
[
  {
    "date": "2025-12-13",
    "totalRevenue": 5000000.00,
    "totalTransactions": 50
  }
]
```

3. Lấy tóm tắt doanh thu
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/revenue/summary?startDate=2025-12-01&endDate=2025-12-31`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: 
  - `startDate` (format: YYYY-MM-DD)
  - `endDate` (format: YYYY-MM-DD)
- Response (raw JSON):
```json
{
  "totalRevenue": 150000000.00,
  "averageDailyRevenue": 5000000.00,
  "totalTransactions": 1500,
  "highestRevenueDay": "2025-12-15",
  "highestRevenueAmount": 10000000.00
}
```

4. Tính lại doanh thu theo ngày
- Method: POST
- URL: `{{baseUrl}}/api/analytics/admin/revenue/daily/{date}/recalculate`
- Headers: `Authorization: Bearer {{accessToken}}`
- Path param: `date` (format: YYYY-MM-DD)
- Response: Status 202 Accepted

### Admin Analytics - User Growth

1. Lấy thống kê tăng trưởng người dùng mới nhất
- Method: GET
- URL: `{{baseUrl}} `
- Headers: `Authorization: Bearer {{accessToken}}`
- Response (raw JSON):
```json
{
  "userGrowthAnalyticsId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "date": "2025-12-13",
  "newUsersCount": 20,
  "activeUsersCount": 500,
  "totalUsers": 1000,
  "retentionRate": 85.5,
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T14:00:00.000000"
}
```

2. Lấy thống kê tăng trưởng theo ngày
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/user-growth/{date}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Path param: `date` (format: YYYY-MM-DD)
- Response: (same as above)

3. Lấy thống kê tăng trưởng theo khoảng thời gian
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/user-growth/period?startDate=2025-12-01&endDate=2025-12-31`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: 
  - `startDate` (format: YYYY-MM-DD)
  - `endDate` (format: YYYY-MM-DD)
- Response: (same as above, array)

4. Lấy tỉ lệ giữ chân trung bình
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/user-growth/retention/average?startDate=2025-12-01&endDate=2025-12-31`
- Headers: `Authorization: Bearer {{accessToken}}`
- Query params: 
  - `startDate` (format: YYYY-MM-DD)
  - `endDate` (format: YYYY-MM-DD)
- Response (raw JSON):
```json
{
  "averageRetentionRate": 85.5,
  "startDate": "2025-12-01",
  "endDate": "2025-12-31"
}
```

5. Lấy tổng số người dùng hoạt động theo ngày
- Method: GET
- URL: `{{baseUrl}}/api/analytics/admin/user-growth/active-users/{date}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Path param: `date` (format: YYYY-MM-DD)
- Response (raw JSON):
```json
{
  "date": "2025-12-13",
  "totalActiveUsers": 500
}
```

6. Tính lại tỉ lệ giữ chân theo ngày
- Method: POST
- URL: `{{baseUrl}}/api/analytics/admin/user-growth/{date}/calculate-retention`
- Headers: `Authorization: Bearer {{accessToken}}`
- Path param: `date` (format: YYYY-MM-DD)
- Response: Status 202 Accepted