package com.se347.paymentservice.dtos;

import lombok.Data;
import java.util.UUID;

@Data
public class VnpayRequest {
    private String amount;
    private UUID userId;
    private UUID courseId;
    private UUID instructorId;
    private String courseSlug;
}