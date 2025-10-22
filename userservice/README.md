# API Endpoints
Base URL: `http://localhost:8006/api/users/profiles`

1) Tạo hồ sơ người dùng
- **POST** `/`
- Body (JSON):
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

2) Lấy hồ sơ theo email hiện tại
- **GET** `/me`

3) Lấy hồ sơ theo `userId`
- **GET** `/{userId}`

4) Cập nhật hồ sơ theo `userId`
- **PUT** `/{userId}`