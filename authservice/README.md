# Postman testcases

1) Signup
- Method: POST
- URL: {{baseUrl}}/api/auth/signup
- Body (raw JSON):
```
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

2) Login
- Method: POST
- URL: {{baseUrl}}/api/auth/login
- Body (raw JSON):
```
{
    "email": "example@gmail.com",
    "password": "12345"
}
```
- Response (raw JSON):
```
{
    "userId": "{userId}",
    "accessToken": "{accessToken}",
    "refreshToken": "{refreshToken}",
    "tokenType": "Bearer",
    "email": "example@gmail.com",
    "role": "USER"
}
```

3) Refresh
- Method: POST
- URL: {{baseUrl}}/api/auth/refresh
- Body (raw JSON):
```
{
  "accessToken": "{Lấy accessToken}"
  "refreshToken": "{Lấy refreshToken}"
}
```
- Response (raw JSON):
```
{
"newAccessToken": "{newAccessToken}",
"refreshToken": "{newRefreshToken}",
}
```

4) Logout
- Method: POST
- URL: {{baseUrl}}/api/auth/logout
- Headers: Authorization: Bearer {{accessToken}}

- Response (String):
```
Logged out successfully
```