package com.se347.contentservice.exception.custom;

import com.se347.contentservice.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class InvalidFileTypeException extends BaseServiceException {
    public InvalidFileTypeException(String message) {
        super(message, "INVALID_FILE_TYPE", HttpStatus.BAD_REQUEST);
    }
}