package com.se347.contentservice.exception;

import com.eduweb.exception.BaseServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileNotFoundException extends BaseServiceException {
    public FileNotFoundException(String message) {
        super(message, "FILE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
