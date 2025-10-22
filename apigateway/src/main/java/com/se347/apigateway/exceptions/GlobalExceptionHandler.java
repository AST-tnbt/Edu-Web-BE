package com.se347.apigateway.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler cho API Gateway
 * 
 * Xử lý tất cả các exception và trả về response thống nhất
 */
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Xử lý BaseGatewayException
     */
    public static Mono<ResponseEntity<Map<String, Object>>> handleBaseGatewayException(
            BaseGatewayException ex, ServerWebExchange exchange) {
        
        logger.error("Gateway Exception: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
        
        Map<String, Object> response = createErrorResponse(ex, exchange);
        return Mono.just(ResponseEntity.status(ex.getHttpStatus()).body(response));
    }
    
    /**
     * Xử lý Exception chung
     */
    public static Mono<ResponseEntity<Map<String, Object>>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        
        logger.error("Unexpected error occurred", ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "INTERNAL_SERVER_ERROR");
        response.put("message", "An unexpected error occurred");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("path", getCurrentPath(exchange));
        response.put("time", LocalDateTime.now().format(formatter));
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
    
    /**
     * Xử lý RuntimeException
     */
    public static Mono<ResponseEntity<Map<String, Object>>> handleRuntimeException(
            RuntimeException ex, ServerWebExchange exchange) {
        
        logger.error("Runtime Exception occurred", ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "RUNTIME_ERROR");
        response.put("message", ex.getMessage() != null ? ex.getMessage() : "Runtime error occurred");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("path", getCurrentPath(exchange));
        response.put("time", LocalDateTime.now().format(formatter));
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
    
    /**
     * Xử lý IllegalArgumentException
     */
    public static Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgumentException(
            IllegalArgumentException ex, ServerWebExchange exchange) {
        
        logger.warn("Illegal Argument: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "INVALID_ARGUMENT");
        response.put("message", ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("path", getCurrentPath(exchange));
        response.put("time", LocalDateTime.now().format(formatter));
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }
    
    /**
     * Tạo error response từ BaseGatewayException
     */
    private static Map<String, Object> createErrorResponse(BaseGatewayException ex, ServerWebExchange exchange) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", ex.getErrorCode());
        response.put("message", ex.getMessage());
        response.put("timestamp", ex.getTimestamp());
        response.put("status", ex.getHttpStatus().value());
        response.put("path", getCurrentPath(exchange));
        response.put("time", LocalDateTime.now().format(formatter));
        
        // Thêm thông tin debug nếu cần
        if (logger.isDebugEnabled()) {
            response.put("exception", ex.getClass().getSimpleName());
            if (ex.getCause() != null) {
                response.put("cause", ex.getCause().getMessage());
            }
        }
        
        return response;
    }
    
    /**
     * Lấy path hiện tại từ exchange
     */
    private static String getCurrentPath(ServerWebExchange exchange) {
        if (exchange != null && exchange.getRequest() != null) {
            return exchange.getRequest().getURI().getPath();
        }
        return "Unknown";
    }
    
    
    /**
     * Tạo error response cho các trường hợp đặc biệt
     */
    public static Map<String, Object> createCustomErrorResponse(
            String errorCode, String message, HttpStatus status, String path) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", errorCode);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", status.value());
        response.put("path", path);
        response.put("time", LocalDateTime.now().format(formatter));
        
        return response;
    }
}
