# Payment Service

## Tổng quan

Payment Service là một microservice xử lý thanh toán qua VNPay gateway. Service này tích hợp với VNPay để tạo payment URL và xử lý IPN (Instant Payment Notification) callback từ VNPay. Sau khi thanh toán thành công, service sẽ publish event qua RabbitMQ để thông báo cho các service khác (như Enrollment Service).

## Tính năng

- ✅ Tạo payment URL từ VNPay
- ✅ Xử lý IPN callback từ VNPay
- ✅ Xác thực chữ ký bảo mật (HMAC SHA512)
- ✅ Publish payment completed event qua RabbitMQ
- ✅ Tích hợp với Eureka Service Discovery
- ✅ Health check endpoint

## Công nghệ sử dụng

- **Framework**: Spring Boot 3.3.4
- **Java Version**: 21
- **Payment Gateway**: VNPay
- **Message Broker**: RabbitMQ
- **Service Discovery**: Netflix Eureka
- **Build Tool**: Maven
- **Container**: Docker

## Cấu trúc dự án

```
paymentservice/
├── src/main/java/com/se347/paymentservice/
│   ├── config/
│   │   ├── RabbitConfig.java          # Cấu hình RabbitMQ
│   │   └── VnpayConfig.java           # Cấu hình VNPay
│   ├── controller/
│   │   └── PaymentController.java     # REST API endpoints
│   ├── dtos/
│   │   ├── PaymentCompletedEvent.java # Event cho RabbitMQ
│   │   ├── PaymentUrlResponse.java    # Response chứa payment URL
│   │   └── VnpayRequest.java           # Request DTO
│   ├── exception/
│   │   └── AmountException.java       # Exception xử lý lỗi amount
│   ├── publisher/
│   │   ├── PaymentPublisher.java      # Interface publisher
│   │   └── PaymentPubliserImpl.java   # Implementation publisher
│   └── service/
│       ├── PaymentService.java         # Service interface
│       └── PaymentServiceImpl.java     # Service implementation
└── src/main/resources/
    └── application.properties          # Application configuration
```

## API Endpoints

### 1. Tạo Payment URL

**Endpoint**: `POST /api/payment`

**Mô tả**: Tạo payment URL từ VNPay để redirect user đến trang thanh toán.

**Request Body**:
```json
{
  "amount": "100000"
}
```

**Response 200 OK**:
```json
{
  "url": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=10000000&vnp_Command=pay&..."
}
```

**Response 400 Bad Request** (Invalid amount):
```json
{
  "message": "Invalid amount format",
  "errorCode": "INVALID_NUMBER",
  "status": "BAD_REQUEST"
}
```

### 2. Xử lý IPN Callback

**Endpoint**: `GET /api/payment/ipn`

**Mô tả**: Endpoint nhận callback từ VNPay sau khi user hoàn tất thanh toán (Instant Payment Notification).

**Query Parameters**: 
- `vnp_Amount`: Số tiền thanh toán
- `vnp_BankCode`: Mã ngân hàng
- `vnp_BankTranNo`: Mã giao dịch tại ngân hàng
- `vnp_CardType`: Loại thẻ
- `vnp_OrderInfo`: Thông tin đơn hàng
- `vnp_PayDate`: Ngày thanh toán
- `vnp_ResponseCode`: Mã phản hồi (00 = thành công)
- `vnp_TmnCode`: Mã website của merchant
- `vnp_TransactionNo`: Mã giao dịch tại VNPay
- `vnp_TransactionStatus`: Trạng thái giao dịch (00 = thành công)
- `vnp_TxnRef`: Mã tham chiếu giao dịch
- `vnp_SecureHash`: Chữ ký bảo mật
- `userId`: UUID của user (custom field)
- `courseId`: UUID của course (custom field)

**Response 200 OK** (Payment thành công):
```json
{
  "RspCode": "00",
  "Message": "Payment confirmed successfully"
}
```

**Response 200 OK** (Payment thất bại):
```json
{
  "RspCode": "02",
  "Message": "Payment failed"
}
```

**Response 200 OK** (Invalid checksum):
```json
{
  "RspCode": "97",
  "Message": "Invalid checksum"
}
```

**Response 200 OK** (Lỗi khác):
```json
{
  "RspCode": "99",
  "Message": "Unknown error: ..."
}
```

## Cấu hình

### Environment Variables

Service sử dụng các biến môi trường sau (được định nghĩa trong `application.properties`):

#### VNPay Configuration
- `PAY_URL`: URL của VNPay payment gateway
- `RETURN_URL`: URL callback sau khi thanh toán
- `TMN_CODE`: Mã website của merchant trên VNPay
- `SECRET_KEY`: Secret key để tạo chữ ký bảo mật
- `API_URL`: API URL của VNPay

#### RabbitMQ Configuration
- `SPRING_RABBITMQ_HOST`: Host của RabbitMQ (default: localhost)
- `SPRING_RABBITMQ_PORT`: Port của RabbitMQ (default: 5672)
- `SPRING_RABBITMQ_USERNAME`: Username RabbitMQ (default: user)
- `SPRING_RABBITMQ_PASSWORD`: Password RabbitMQ (default: password)
- `APP_RABBITMQ_EXCHANGE_ENROLLMENT_PAYMENT`: Exchange name (default: enrollment_payment.exchange)
- `APP_RABBITMQ_ROUTING_KEY_PAYMENT_COMPLETED`: Routing key (default: payment.completed)
- `APP_RABBITMQ_QUEUE_PAYMENT_COMPLETED`: Queue name (default: payment.completed.queue)

#### Eureka Configuration
- `PAYMENTSERVICE_EUREKA_SERVER_URL`: URL của Eureka server (default: http://localhost:8761/eureka/)
- `EUREKA_SERVER_URL`: Fallback Eureka server URL

### Port

Service chạy trên port **8010** (có thể thay đổi trong `application.properties`).

## Postman Test Cases

### Collection: Payment Service API

#### Test Case 1: Tạo Payment URL - Thành công

**Request**:
- **Method**: `POST`
- **URL**: `http://localhost:8010/api/payment`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (raw JSON):
  ```json
  {
    "amount": "100000"
  }
  ```

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**:
  ```json
  {
    "url": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=10000000&..."
  }
  ```
- **Assertions**:
  - Status code is 200
  - Response contains "url" field
  - URL starts with VNPay domain

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has url field", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('url');
});

pm.test("URL is valid VNPay URL", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.url).to.include('vnpayment.vn');
});
```

---

#### Test Case 2: Tạo Payment URL - Amount không hợp lệ (không phải số)

**Request**:
- **Method**: `POST`
- **URL**: `http://localhost:8010/api/payment`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (raw JSON):
  ```json
  {
    "amount": "invalid_amount"
  }
  ```

**Expected Response**:
- **Status Code**: `400 Bad Request`
- **Response Body**:
  ```json
  {
    "message": "...",
    "errorCode": "INVALID_NUMBER",
    "status": "BAD_REQUEST"
  }
  ```

**Postman Test Script**:
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error code is INVALID_NUMBER", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.errorCode).to.eql("INVALID_NUMBER");
});
```

---

#### Test Case 3: Tạo Payment URL - Amount rỗng

**Request**:
- **Method**: `POST`
- **URL**: `http://localhost:8010/api/payment`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (raw JSON):
  ```json
  {
    "amount": ""
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

#### Test Case 4: Tạo Payment URL - Thiếu field amount

**Request**:
- **Method**: `POST`
- **URL**: `http://localhost:8010/api/payment`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (raw JSON):
  ```json
  {}
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

#### Test Case 5: IPN Callback - Payment thành công

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8010/api/payment/ipn`
- **Query Params** (giả lập từ VNPay):
  ```
  vnp_Amount=10000000
  vnp_BankCode=NCB
  vnp_BankTranNo=VNP12345678
  vnp_CardType=ATM
  vnp_OrderInfo=Thanh toan don hang:12345678
  vnp_PayDate=20231201120000
  vnp_ResponseCode=00
  vnp_TmnCode=YOUR_TMN_CODE
  vnp_TransactionNo=12345678
  vnp_TransactionStatus=00
  vnp_TxnRef=12345678
  vnp_SecureHash=YOUR_CALCULATED_HASH
  userId=550e8400-e29b-41d4-a716-446655440000
  courseId=660e8400-e29b-41d4-a716-446655440000
  ```

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**:
  ```json
  {
    "RspCode": "00",
    "Message": "Payment confirmed successfully"
  }
  ```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Payment confirmed successfully", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.RspCode).to.eql("00");
    pm.expect(jsonData.Message).to.include("successfully");
});
```

**Lưu ý**: Để test IPN callback, bạn cần:
1. Tính toán `vnp_SecureHash` đúng cách dựa trên secret key
2. Sắp xếp các tham số theo thứ tự alphabet
3. Encode các giá trị trước khi tạo hash

---

#### Test Case 6: IPN Callback - Payment thất bại (ResponseCode != 00)

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8010/api/payment/ipn`
- **Query Params**:
  ```
  vnp_Amount=10000000
  vnp_ResponseCode=07
  vnp_TransactionStatus=00
  vnp_TxnRef=12345678
  vnp_SecureHash=YOUR_CALCULATED_HASH
  userId=550e8400-e29b-41d4-a716-446655440000
  courseId=660e8400-e29b-41d4-a716-446655440000
  ...
  ```

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**:
  ```json
  {
    "RspCode": "02",
    "Message": "Payment failed"
  }
  ```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Payment failed", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.RspCode).to.eql("02");
    pm.expect(jsonData.Message).to.include("failed");
});
```

---

#### Test Case 7: IPN Callback - Invalid checksum

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8010/api/payment/ipn`
- **Query Params**:
  ```
  vnp_Amount=10000000
  vnp_ResponseCode=00
  vnp_TransactionStatus=00
  vnp_TxnRef=12345678
  vnp_SecureHash=INVALID_HASH
  userId=550e8400-e29b-41d4-a716-446655440000
  courseId=660e8400-e29b-41d4-a716-446655440000
  ...
  ```

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**:
  ```json
  {
    "RspCode": "97",
    "Message": "Invalid checksum"
  }
  ```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Invalid checksum detected", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.RspCode).to.eql("97");
    pm.expect(jsonData.Message).to.include("checksum");
});
```

---

#### Test Case 8: IPN Callback - Thiếu tham số bắt buộc

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8010/api/payment/ipn`
- **Query Params**:
  ```
  vnp_Amount=10000000
  ```

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**:
  ```json
  {
    "RspCode": "99",
    "Message": "Exception: ..."
  }
  ```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Exception occurred", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.RspCode).to.eql("99");
});
```

---

#### Test Case 9: Health Check

**Request**:
- **Method**: `GET`
- **URL**: `http://localhost:8010/actuator/health`

**Expected Response**:
- **Status Code**: `200 OK`
- **Response Body**:
  ```json
  {
    "status": "UP"
  }
  ```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Service is UP", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.status).to.eql("UP");
});
```

---

## Import Postman Collection

Bạn có thể tạo một Postman Collection với các test cases trên. Dưới đây là JSON format để import vào Postman:

```json
{
  "info": {
    "name": "Payment Service API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Create Payment URL - Success",
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
          "raw": "{\n  \"amount\": \"100000\"\n}"
        },
        "url": {
          "raw": "http://localhost:8010/api/payment",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8010",
          "path": ["api", "payment"]
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
              "pm.test(\"Response has url field\", function () {",
              "    var jsonData = pm.response.json();",
              "    pm.expect(jsonData).to.have.property('url');",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "Create Payment URL - Invalid Amount",
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
          "raw": "{\n  \"amount\": \"invalid_amount\"\n}"
        },
        "url": {
          "raw": "http://localhost:8010/api/payment",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8010",
          "path": ["api", "payment"]
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test(\"Status code is 400\", function () {",
              "    pm.response.to.have.status(400);",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "IPN Callback - Success",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8010/api/payment/ipn?vnp_Amount=10000000&vnp_ResponseCode=00&vnp_TransactionStatus=00&vnp_TxnRef=12345678&vnp_SecureHash=YOUR_HASH&userId=550e8400-e29b-41d4-a716-446655440000&courseId=660e8400-e29b-41d4-a716-446655440000",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8010",
          "path": ["api", "payment", "ipn"],
          "query": [
            {
              "key": "vnp_Amount",
              "value": "10000000"
            },
            {
              "key": "vnp_ResponseCode",
              "value": "00"
            },
            {
              "key": "vnp_TransactionStatus",
              "value": "00"
            },
            {
              "key": "vnp_TxnRef",
              "value": "12345678"
            },
            {
              "key": "vnp_SecureHash",
              "value": "YOUR_HASH"
            },
            {
              "key": "userId",
              "value": "550e8400-e29b-41d4-a716-446655440000"
            },
            {
              "key": "courseId",
              "value": "660e8400-e29b-41d4-a716-446655440000"
            }
          ]
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
              "pm.test(\"Payment confirmed\", function () {",
              "    var jsonData = pm.response.json();",
              "    pm.expect(jsonData.RspCode).to.eql(\"00\");",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8010/actuator/health",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8010",
          "path": ["actuator", "health"]
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
              "pm.test(\"Service is UP\", function () {",
              "    var jsonData = pm.response.json();",
              "    pm.expect(jsonData.status).to.eql(\"UP\");",
              "});"
            ]
          }
        }
      ]
    }
  ]
}
```

## Cách chạy

### Chạy với Maven

```bash
cd paymentservice
mvn clean install
mvn spring-boot:run
```

### Chạy với Docker

```bash
docker build -t paymentservice:latest -f paymentservice/Dockerfile .
docker run -p 8010:8010 \
  -e PAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html \
  -e RETURN_URL=http://localhost:8010/api/payment/ipn \
  -e TMN_CODE=YOUR_TMN_CODE \
  -e SECRET_KEY=YOUR_SECRET_KEY \
  paymentservice:latest
```

### Chạy với Docker Compose

Service được định nghĩa trong `docker-compose.yml` ở root project. Chạy:

```bash
docker-compose up paymentservice
```

## Lưu ý

1. **VNPay Configuration**: Cần cấu hình đúng các thông tin VNPay (TMN_CODE, SECRET_KEY) trong environment variables.

2. **IPN Callback**: Endpoint `/api/payment/ipn` cần được expose ra ngoài để VNPay có thể gọi callback. Trong môi trường production, cần sử dụng HTTPS.

3. **Security**: Secret key phải được bảo mật, không commit vào code. Sử dụng environment variables hoặc secret management service.

4. **RabbitMQ**: Đảm bảo RabbitMQ đang chạy và cấu hình đúng connection settings.

5. **Eureka**: Service sẽ tự động đăng ký với Eureka nếu Eureka server đang chạy.

## Tích hợp với các service khác

Payment Service publish event `PaymentCompletedEvent` qua RabbitMQ khi thanh toán thành công. Event này được consume bởi Enrollment Service để xử lý enrollment sau khi payment hoàn tất.

**Event Structure**:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "courseId": "660e8400-e29b-41d4-a716-446655440000"
}
```

## Troubleshooting

### Service không start được
- Kiểm tra port 8010 có đang bị chiếm dụng không
- Kiểm tra các environment variables đã được set đúng chưa

### IPN callback không hoạt động
- Kiểm tra RETURN_URL có đúng không
- Kiểm tra secret key có khớp với VNPay không
- Kiểm tra firewall/network có cho phép VNPay gọi callback không

### RabbitMQ connection failed
- Kiểm tra RabbitMQ đang chạy không
- Kiểm tra username/password có đúng không
- Kiểm tra host/port có đúng không

## Tác giả

SE347 - Payment Service Team

