package com.se347.paymentservice.controller;

import com.se347.paymentservice.dtos.PaymentUrlResponse;
import com.se347.paymentservice.dtos.VnpayRequest;
import com.se347.paymentservice.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PaymentUrlResponse> createPayment(@RequestBody VnpayRequest paymentRequest) {
        PaymentUrlResponse paymentUrl = service.createPayment(paymentRequest);
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> handleIpn(@RequestParam Map<String, String> allParams) {
        try {
            Map<String, String> response = service.handleIpn(allParams);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("RspCode", "99");
            error.put("Message", "Unknown error: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

}
