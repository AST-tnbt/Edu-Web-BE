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
    "role": "[STUDENT]",
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

1. Tạo hồ sơ người dùng
- Method: POST
- URL: `{{baseUrl}}/api/users/profiles`
- Headers: `Authorization: Bearer {{accessToken}}`
- Body (raw JSON):
```json
{
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "fullName": "Nguyen Van A",
  "avatarUrl": "https://example.com/a.jpg",
  "bio": "Hello there",
  "phoneNumber": "+84901234567",
  "address": "Hanoi, Vietnam"
}
```
- Response:
```json
{
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "fullName": "Nguyen Van A",
  "avatarUrl": "https://example.com/a.jpg",
  "bio": "Hello there",
  "phoneNumber": "+84901234567",
  "address": "Hanoi, Vietnam",
  "createdAt": "2025-10-31T10:11:39.648084762",
  "updatedAt": "2025-10-31T10:11:39.648100429"
}
```

2. Lấy hồ sơ theo email hiện tại
- Method: GET
- URL: `{{baseUrl}}/api/users/profiles/me`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response:
```json
{
    "userId": "7e26ae87-6800-4a01-92e1-129fda1bb33d",
    "fullName": "Nguyen Van A",
    "avatarUrl": "https://example.com/a.jpg",
    "bio": "Hello there",
    "phoneNumber": "+84901234567",
    "address": "Hanoi, Vietnam",
    "createdAt": "2025-10-31T10:11:39.659688",
    "updatedAt": "2025-10-31T10:11:39.659696"
}
```

3. Lấy hồ sơ theo `userId`
- Method: GET
- URL: `{{baseUrl}}/api/users/profiles/{userId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Response:
```json
{
    "userId": "7e26ae87-6800-4a01-92e1-129fda1bb33d",
    "fullName": "Nguyen Van A",
    "avatarUrl": "https://example.com/a.jpg",
    "bio": "Hello there",
    "phoneNumber": "+84901234567",
    "address": "Hanoi, Vietnam",
    "createdAt": "2025-10-31T10:11:39.659688",
    "updatedAt": "2025-10-31T10:11:39.659696"
}
```

4. Cập nhật hồ sơ theo `userId`
- Method: PUT
- URL: `{{baseUrl}}/api/users/profiles/{userId}`
- Headers: `Authorization: Bearer {{accessToken}}`
- Body (raw JSON):
```json
{
    "fullName": "Nguyen Van Hehe",
    "avatarUrl": "https://example.com/a.jpg",
    "bio": "Hello there",
    "phoneNumber": "+84901234567",
    "address": "Hanoi, Vietnam"
}
```
- Response:
```json
{
    "userId": "7e26ae87-6800-4a01-92e1-129fda1bb33d",
    "fullName": "Nguyen Van Hehe",
    "avatarUrl": "https://example.com/a.jpg",
    "bio": "Hello there",
    "phoneNumber": "+84901234567",
    "address": "Hanoi, Vietnam",
    "createdAt": "2025-10-31T10:11:39.659688",
    "updatedAt": "2025-10-31T10:24:32.987335342"
}
```