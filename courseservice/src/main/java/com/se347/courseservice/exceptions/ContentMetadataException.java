package com.se347.courseservice.exceptions;

public class ContentMetadataException extends RuntimeException {
    public ContentMetadataException(String message) {
        super(message);
    }

    public static class ContentNotFoundException extends ContentMetadataException {
        public ContentNotFoundException(String contentId) {
            super("Content not found with ID: " + contentId);
        }
    }
}


