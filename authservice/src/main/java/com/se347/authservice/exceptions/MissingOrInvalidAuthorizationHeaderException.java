package com.se347.authservice.exceptions;

public class MissingOrInvalidAuthorizationHeaderException extends RuntimeException {
    public MissingOrInvalidAuthorizationHeaderException(String message) {
        super(message);
    }
}


