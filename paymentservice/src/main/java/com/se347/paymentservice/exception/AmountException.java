package com.se347.paymentservice.exception;

import com.eduweb.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class AmountException extends BaseServiceException {
    public AmountException(String message) {
        super(message, "INVALID_NUMBER", HttpStatus.BAD_REQUEST);
    }
}
