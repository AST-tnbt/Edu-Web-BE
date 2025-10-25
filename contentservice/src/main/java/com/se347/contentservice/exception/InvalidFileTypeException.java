package com.se347.contentservice.exception;

import com.eduweb.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class InvalidFileTypeException extends BaseServiceException {
    public InvalidFileTypeException(String message) {
        super(message, "INVALID_FILE_TYPE", HttpStatus.BAD_REQUEST);
    }
}