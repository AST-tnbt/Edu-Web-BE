package com.se347.paymentservice.service;

import com.se347.paymentservice.config.VnpayConfig;
import com.se347.paymentservice.dtos.PaymentCompletedEvent;
import com.se347.paymentservice.dtos.PaymentUrlResponse;
import com.se347.paymentservice.dtos.VnpayRequest;
import com.se347.paymentservice.exception.AmountException;
import com.se347.paymentservice.publisher.PaymentPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{

    private final VnpayConfig vnpayConfig;
    private final PaymentPublisher paymentPublisher;

    @Override
    @Transactional
    public PaymentUrlResponse createPayment(VnpayRequest paymentRequest) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";

        long amount;
        try {
            amount = Long.parseLong(paymentRequest.getAmount()) * 100;
        } catch (NumberFormatException e) {
            throw new AmountException(e.getMessage());
        }

        String vnp_TxnRef = vnpayConfig.getRandomNumber(8);
        String vnp_IpAddr = "127.0.0.1";
        String vnp_TmnCode = vnpayConfig.tmnCode;
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.returnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append('&');
                hashData.append('&');
            }
        }

        if (!query.isEmpty())
            query.setLength(query.length() - 1);
        if (!hashData.isEmpty())
            hashData.setLength(hashData.length() - 1);

        String vnp_SecureHash = vnpayConfig.hmacSHA512(vnpayConfig.secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        return new PaymentUrlResponse(vnpayConfig.payUrl + "?" + query);
    }

    @Transactional
    public Map<String, String> handleIpn(Map<String, String> vnpParams) {
        Map<String, String> response = new HashMap<>();

        try {
            String secureHash = vnpParams.get("vnp_SecureHash");
            vnpParams.remove("vnp_SecureHash");
            vnpParams.remove("vnp_SecureHashType");

            String vnpTxnRef = vnpParams.get("vnp_TxnRef");
            UUID userId = UUID.fromString(vnpParams.get("userId"));
            UUID courseId = UUID.fromString(vnpParams.get("courseId"));
            UUID instructorId = UUID.fromString(vnpParams.get("instructorId"));
            String courseSlug = vnpParams.get("courseSlug");
            BigDecimal amount = new BigDecimal(vnpParams.get("amount"));
            
            vnpParams.remove("userId");
            vnpParams.remove("courseId");
            vnpParams.remove("instructorId");
            vnpParams.remove("courseSlug");
            vnpParams.remove("amount");

            // Sắp xếp tham số theo tên (A-Z)
            Map<String, String> sortedParams = new TreeMap<>(vnpParams);

            // Tạo chuỗi dữ liệu ký (đúng chuẩn: encode value trước khi nối)
            StringBuilder signDataBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value != null && !value.isEmpty()) {
                    if (!signDataBuilder.isEmpty()) {
                        signDataBuilder.append("&");
                    }
                    signDataBuilder
                            .append(key)
                            .append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                }
            }
            String signData = signDataBuilder.toString();

            // Sinh chữ ký mới từ secretKey
            String hashSecret = vnpayConfig.secretKey; // secretKey từ config của bạn
            String calculatedHash = vnpayConfig.hmacSHA512(hashSecret, signData);

            // So sánh chữ ký
            if (!calculatedHash.equalsIgnoreCase(secureHash)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid checksum");
                return response;
            }

            String responseCode = vnpParams.get("vnp_ResponseCode");
            String transactionStatus = vnpParams.get("vnp_TransactionStatus");

            if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                LocalDateTime now = LocalDateTime.now();
                PaymentCompletedEvent paymentCompletedEvent = PaymentCompletedEvent.builder()
                        .eventId(UUID.randomUUID())
                        .paymentId(UUID.randomUUID()) // Generate payment ID for tracking
                        .userId(userId)
                        .courseId(courseId)
                        .instructorId(instructorId)
                        .amount(amount)
                        .courseSlug(courseSlug)
                        .vnpTxnRef(vnpTxnRef)
                        .completedAt(now) // Set completion time
                        .occurredAt(now)
                        .build();
                
                paymentPublisher.publishPaymentSuccessEvent(paymentCompletedEvent);
                
                response.put("RspCode", "00");
                response.put("Message", "Payment confirmed successfully");
            } else {
                response.put("RspCode", "02");
                response.put("Message", "Payment failed");
            }

            return response;

        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Exception: " + e.getMessage());
            return response;
        }
    }
}
