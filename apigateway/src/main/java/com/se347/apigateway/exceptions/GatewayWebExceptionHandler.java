package com.se347.apigateway.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * WebExceptionHandler cho Spring WebFlux
 * 
 * Xử lý tất cả các exception trong reactive pipeline
 */
@Component
@Order(-2) // Chạy trước DefaultErrorWebExceptionHandler
public class GatewayWebExceptionHandler implements WebExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewayWebExceptionHandler.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public @org.springframework.lang.NonNull Mono<Void> handle(@org.springframework.lang.NonNull ServerWebExchange exchange, @org.springframework.lang.NonNull Throwable ex) {
        logger.error("Exception occurred in WebFlux pipeline", ex);
        
        ServerHttpResponse response = exchange.getResponse();
        
        // Xác định HTTP status và error response
        HttpStatus status = determineHttpStatus(ex);
        Map<String, Object> errorResponse = createErrorResponse(ex, exchange, status);
        
        // Set response headers
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().add("X-Error-Source", "api-gateway");
        
        // Convert error response to JSON
        String jsonResponse = convertToJson(errorResponse);
        
        // Write response
        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8)))
        );
    }
    
    /**
     * Xác định HTTP status từ exception
     */
    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof BaseGatewayException) {
            return ((BaseGatewayException) ex).getHttpStatus();
        }
        
        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        
        if (ex instanceof SecurityException) {
            return HttpStatus.FORBIDDEN;
        }
        
        if (ex instanceof RuntimeException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    /**
     * Tạo error response
     */
    private Map<String, Object> createErrorResponse(Throwable ex, ServerWebExchange exchange, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        
        if (ex instanceof BaseGatewayException) {
            BaseGatewayException gatewayEx = (BaseGatewayException) ex;
            response.put("error", gatewayEx.getErrorCode());
            response.put("message", gatewayEx.getMessage());
            response.put("timestamp", gatewayEx.getTimestamp());
        } else {
            response.put("error", getErrorCode(ex));
            response.put("message", ex.getMessage() != null ? ex.getMessage() : "An error occurred");
            response.put("timestamp", System.currentTimeMillis());
        }
        
        response.put("status", status.value());
        response.put("path", getCurrentPath(exchange));
        response.put("time", LocalDateTime.now().format(formatter));
        response.put("source", "api-gateway");
        
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
     * Lấy error code từ exception
     */
    private String getErrorCode(Throwable ex) {
        if (ex instanceof IllegalArgumentException) {
            return "INVALID_ARGUMENT";
        }
        if (ex instanceof SecurityException) {
            return "SECURITY_ERROR";
        }
        if (ex instanceof RuntimeException) {
            return "RUNTIME_ERROR";
        }
        return "UNKNOWN_ERROR";
    }
    
    /**
     * Lấy path hiện tại
     */
    private String getCurrentPath(ServerWebExchange exchange) {
        if (exchange != null && exchange.getRequest() != null) {
            return exchange.getRequest().getURI().getPath();
        }
        return "Unknown";
    }
    
    /**
     * Convert Map to JSON string (simple implementation)
     */
    private String convertToJson(Map<String, Object> response) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : response.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else {
                json.append(value);
            }
            
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Escape JSON string
     */
    private String escapeJson(String str) {
        if (str == null) return "null";
        
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\b", "\\b")
                 .replace("\f", "\\f")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
}