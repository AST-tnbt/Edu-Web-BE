# Script setup container

## MySQL (map cổng 3307 -> 3306)
docker run -d --name authservice-mysql -e MYSQL_DATABASE=authdb -e MYSQL_ROOT_PASSWORD=root -e MYSQL_USER=authuser -e MYSQL_PASSWORD=authpass -p 3307:3306 -v auth_mysql_data:/var/lib/mysql mysql:8.4

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
    "userId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlQGdtYWlsLmNvbSIsInJvbGVzIjpbIlNUVURFTlQiXSwiaWF0IjoxNzYwMTQ5NDcwLCJleHAiOjE3NjAyMzU4NzB9.L9Rg4dmOtzWtL9EbXwF6zB6GiW2yc1uLfaOW91UzJLo",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlQGdtYWlsLmNvbSIsImlhdCI6MTc2MDE0OTQ3MCwiZXhwIjoxNzYwNTgxNDcwfQ.um6ljpblLhK8IPxFY8uDpYt__GUS7hhNmE6Ie5rKwsw",
    "tokenType": "Bearer",
    "email": "example@gmail.com",
    "role": "[STUDENT]"
}
```

3) Refresh
- Method: POST
- URL: {{baseUrl}}/api/auth/refresh
- Body (raw JSON):
```
{
  "refreshToken": "{Lấy refreshToken}"
}
```
- Response (raw JSON):
```
{
"userId": "30a2cc2f-7d29-4cd9-bd60-f26244a15a78",
"accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlQGdtYWlsLmNvbSIsInJvbGVzIjpbIlNUVURFTlQiXSwiaWF0IjoxNzYwMTQ5NjIwLCJleHAiOjE3NjAyMzYwMjB9.2MOMEcC7MGUNCBFB54cD6KdJuaVm4J3gVQBrHnZEhQI",
"refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlQGdtYWlsLmNvbSIsImlhdCI6MTc2MDE0OTQ3MCwiZXhwIjoxNzYwNTgxNDcwfQ.um6ljpblLhK8IPxFY8uDpYt__GUS7hhNmE6Ie5rKwsw",
"tokenType": "Bearer",
"email": "example@gmail.com",
"role": "[STUDENT]"
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