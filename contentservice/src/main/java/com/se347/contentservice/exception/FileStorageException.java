package com.se347.contentservice.exception;

import com.eduweb.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class FileStorageException extends BaseServiceException {
    public FileStorageException(String message, Throwable cause) {
        super(message, "FILE_STORAGE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        initCause(cause);
    }
}
