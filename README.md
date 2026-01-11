# Project structure
The project currently includes several modules (services):
1. API Gateway
2. Auth service
3. User service
4. Course service
5. Content service
6. Enrollment service
7. Payment service  

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
- URL: `{{baseUrl}}/api/courses/id/{courseId}/sections/id/{sectionId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

3. Lấy section theo slug
- Method: GET
- URL: `{{baseUrl}}/api/courses/slug/{courseSlug}/sections/slug/{sectionSlug}`
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
- URL: `{{baseUrl}}/api/courses/id/{courseId}/sections/id/{sectionId}/lessons/id/{lessonId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

3. Lấy lesson theo slug
- Method: GET
- URL: `{{baseUrl}}/api/courses/slug/{courseSlug}/sections/slug/{sectionSlug}/lessons/slug/{lessonSlug}`
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
- URL: `{{baseUrl}}/api/courses/id/{courseId}/sections/id/{sectionId}/lessons`
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
- URL: `{{baseUrl}}/api/courses/slug/{courseSlug}/sections/slug/{sectionSlug}/lessons`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

### Content Metadata

1. Tạo content metadata cho lesson
- Method: POST
- URL: `{{baseUrl}}/api/contents`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Body (raw JSON):
```json
{
  "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "contentType": "VIDEO",
  "title": "Introduction Video",
  "contentUrl": "https://example.com/video.mp4",
  "textContent": null,
  "orderIndex": 1,
  "status": "PUBLISHED"
}
```
- Response (raw JSON):
```json
{
  "contentId": "d4e5f6a7-b8c9-0123-def4-234567890123",
  "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "contentType": "VIDEO",
  "title": "Introduction Video",
  "contentUrl": "https://example.com/video.mp4",
  "textContent": null,
  "orderIndex": 1,
  "status": "PUBLISHED",
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T13:46:52.048937"
}
```

2. Lấy content metadata theo ID
- Method: GET
- URL: `{{baseUrl}}/api/contents/{contentId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

3. Cập nhật content metadata
- Method: PUT
- URL: `{{baseUrl}}/api/contents/{contentId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Body (raw JSON):
```json
{
  "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "contentType": "VIDEO",
  "title": "Updated Introduction Video",
  "contentUrl": "https://example.com/video2.mp4",
  "textContent": null,
  "orderIndex": 1,
  "status": "PUBLISHED"
}
```
- Response: (same as above)

4. Lấy tất cả content metadata của lesson
- Method: GET
- URL: `{{baseUrl}}/api/contents/lesson/{lessonId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response (raw JSON):
```json
[
  {
    "contentId": "d4e5f6a7-b8c9-0123-def4-234567890123",
    "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "contentType": "VIDEO",
    "title": "Introduction Video",
    "contentUrl": "https://example.com/video.mp4",
    "textContent": null,
    "orderIndex": 1,
    "status": "PUBLISHED",
    "createdAt": "2025-12-13T13:46:52.048922",
    "updatedAt": "2025-12-13T13:46:52.048937"
  }
]
```

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

3. Lấy enrollments theo student ID
- Method: GET
- URL: `{{baseUrl}}/api/enrollments/student/id/{studentId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Response (raw JSON):
```json
[
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
]
```

4. Lấy enrollments theo course ID
- Method: GET
- URL: `{{baseUrl}}/api/courses/id/{courseId}/enrollments`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

5. Lấy enrollments theo course ID và student ID
- Method: GET
- URL: `{{baseUrl}}/api/courses/id/{courseId}/enrollments/student/id/{studentId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

6. Lấy các khóa học của tôi
- Method: GET
- URL: `{{baseUrl}}/api/enrollments/my-courses`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Response: (same as above)

7. Cập nhật enrollment
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

1. Tạo course progress
- Method: POST
- URL: `{{baseUrl}}/api/course-progress`
- Headers: `Authorization: Bearer {{accessToken}}`
- Body (raw JSON):
```json
{
  "enrollmentId": "e5f6a7b8-c9d0-1234-ef56-789012345678",
  "lessonsCompleted": 5,
  "totalLessons": 10
}
```
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

2. Lấy course progress theo ID
- Method: GET
- URL: `{{baseUrl}}/api/course-progress/id/{courseProgressId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Response: (same as above)

3. Lấy course progress theo enrollment ID
- Method: GET
- URL: `{{baseUrl}}/api/course-progress/enrollment/id/{enrollmentId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)

4. Cập nhật course progress
- Method: PUT
- URL: `{{baseUrl}}/api/course-progress/id/{courseProgressId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Body (raw JSON):
```json
{
  "enrollmentId": "e5f6a7b8-c9d0-1234-ef56-789012345678",
  "lessonsCompleted": 10,
  "totalLessons": 10
}
```
- Response (raw JSON):
```json
{
  "courseProgressId": "f6a7b8c9-d0e1-2345-f678-901234567890",
  "enrollmentId": "e5f6a7b8-c9d0-1234-ef56-789012345678",
  "overallProgress": 100.0,
  "lessonsCompleted": 10,
  "totalLessons": 10,
  "isCourseCompleted": true,
  "courseCompletedAt": "2025-12-13T14:00:00.000000",
  "createdAt": "2025-12-13T13:46:52.048922",
  "updatedAt": "2025-12-13T14:00:00.000000"
}
```

### Learning Progress

1. Tạo learning progress
- Method: POST
- URL: `{{baseUrl}}/api/learning-progress`
- Headers: `Authorization: Bearer {{accessToken}}`
- Body (raw JSON):
```json
{
  "enrollmentId": "e5f6a7b8-c9d0-1234-ef56-789012345678",
  "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "isCompleted": false
}
```
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

2. Lấy learning progress theo ID
- Method: GET
- URL: `{{baseUrl}}/api/learning-progress/id/{learningProgressId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Response: (same as above)

3. Lấy learning progress theo enrollment ID
- Method: GET
- URL: `{{baseUrl}}/api/learning-progress/enrollment/id/{enrollmentId}`
- Headers: `Authorization: Bearer {{accessToken}}`
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

4. Lấy learning progress theo lesson ID và enrollment ID
- Method: GET
- URL: `{{baseUrl}}/api/learning-progress/lesson/id/{lessonId}/enrollment/id/{enrollmentId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Response: (same as above, single object)

5. Cập nhật learning progress
- Method: PUT
- URL: `{{baseUrl}}/api/learning-progress/id/{learningProgressId}`
- Headers: 
  - `Authorization: Bearer {{accessToken}}`
  - `X-User-Id: {userId}`
- Body (raw JSON):
```json
{
  "enrollmentId": "e5f6a7b8-c9d0-1234-ef56-789012345678",
  "lessonId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "isCompleted": true
}
```
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

6. Đánh dấu lesson hoàn thành
- Method: POST
- URL: `{{baseUrl}}/api/learning-progress/lesson/id/{lessonId}/enrollment/id/{enrollmentId}/complete`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response: (same as above)