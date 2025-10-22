## Chạy API Gateway

Thiết lập biến môi trường (tùy chọn, khuyến nghị):
```powershell
# Cổng HTTP
$env:APIGATEWAY_SERVER_PORT = "8080"

# JWT/HMAC
$env:APIGATEWAY_JWT_SECRET = "gw-jwt-secret"
$env:APIGATEWAY_HMAC_SECRET = "gw-hmac-secret"

# Eureka & Redis
$env:APIGATEWAY_EUREKA_SERVER_URL = "http://localhost:8761/eureka/"
$env:APIGATEWAY_REDIS_HOST = "localhost"
$env:APIGATEWAY_REDIS_PORT = "6379"

# Rate limit (tùy chọn)
$env:APIGATEWAY_RATE_LIMIT_REPLENISH_RATE = "10"
$env:APIGATEWAY_RATE_LIMIT_BURST_CAPACITY = "20"
$env:APIGATEWAY_RATE_LIMIT_REQUESTED_TOKENS = "1"

 # Chạy
 mvn -pl apigateway spring-boot:run
```

Chạy file JAR sau khi build:
```powershell
java -jar apigateway\target\apigateway-0.0.1-SNAPSHOT.jar
```

---

## Chạy Auth Service

Thiết lập biến môi trường (tối thiểu nên đặt JWT/HMAC và DB):
```powershell
# Cổng HTTP
$env:AUTHSERVICE_SERVER_PORT = "8005"

# JWT/HMAC
$env:AUTHSERVICE_JWT_SECRET = "auth-jwt-secret"
$env:AUTHSERVICE_HMAC_SECRET = "auth-hmac-secret"

# Datasource (MySQL)
$env:AUTHSERVICE_DATASOURCE_URL = "jdbc:mysql://localhost:3306/authdb?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true"
$env:AUTHSERVICE_DATASOURCE_USERNAME = "authuser"
$env:AUTHSERVICE_DATASOURCE_PASSWORD = "authpass"

# Redis (tùy chọn cho token blacklist, v.v.)
$env:AUTHSERVICE_REDIS_HOST = "localhost"
$env:AUTHSERVICE_REDIS_PORT = "6379"

# Eureka
$env:AUTHSERVICE_EUREKA_SERVER_URL = "http://localhost:8761/eureka/"

 # Chạy
 mvn -pl authservice spring-boot:run
```

Chạy file JAR sau khi build:
```powershell
java -jar authservice\target\authservice-0.0.1-SNAPSHOT.jar
```

---

## Chạy User Service

Thiết lập biến môi trường (tối thiểu DB và HMAC):
```powershell
# Cổng HTTP
$env:USERSERVICE_SERVER_PORT = "8006"

# HMAC
$env:USERSERVICE_HMAC_SECRET = "user-hmac-secret"

# Datasource (MySQL)
$env:USERSERVICE_DATASOURCE_URL = "jdbc:mysql://localhost:3306/usersdb?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true"
$env:USERSERVICE_DATASOURCE_USERNAME = "usersvc"
$env:USERSERVICE_DATASOURCE_PASSWORD = "usersvcpass"

# Eureka
$env:USERSERVICE_EUREKA_SERVER_URL = "http://localhost:8761/eureka/"

 # Chạy
 mvn -pl userservice spring-boot:run
```

Chạy file JAR sau khi build:
```powershell
java -jar userservice\target\userservice-0.0.1-SNAPSHOT.jar
```

---