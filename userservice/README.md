# API Endpoints
Base URL: `http://localhost:8006/api/users/profiles`

1) Tạo hồ sơ người dùng
- **POST** `/`
- Body (JSON):
```json
{
  "userId": "{userId}",
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