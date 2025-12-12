package com.se347.paymentservice.service;

import com.se347.paymentservice.dtos.PaymentUrlResponse;
import com.se347.paymentservice.dtos.VnpayRequest;
import java.util.Map;

public interface PaymentService {

    /**
     * Create a VNPay payment URL for the given payment request.
     *
     * @param paymentRequest the payment request data
     * @return a URL string to redirect the user to VNPay payment gateway
     */
    PaymentUrlResponse createPayment(VnpayRequest paymentRequest);

    /**
     * Handle the return response from VNPay after user completes payment.
     *
     * @param vnpParams the response code from VNPay
     * @return a ResponseEntity indicating success or failure message
     */

    Map<String, String> handleIpn(Map<String, String> vnpParams);

}
