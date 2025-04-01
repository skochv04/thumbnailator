package com.example.thumbnailator.error;

public class ImageForIdNotFoundException extends RuntimeException {
    public ImageForIdNotFoundException(String message) {
        super(message);
    }
}
