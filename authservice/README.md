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
  "email": "{{email}}",
  "password": "{{password}}"
}
```

2) Login
- Method: POST
- URL: {{baseUrl}}/api/auth/login
- Body (raw JSON):
```
{
  "email": "{{email}}",
  "password": "{{password}}"
}
```

3) Refresh
- Method: POST
- URL: {{baseUrl}}/api/auth/refresh
- Body (raw JSON):
```
{
  "refreshToken": "{{refreshToken}}"
}
```
4) Logout
- Method: POST
- URL: {{baseUrl}}/api/auth/logout
- Headers: Authorization: Bearer {{accessToken}}