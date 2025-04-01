package com.example.thumbnailator.error;

public class InvalidZipFile extends RuntimeException {
    public InvalidZipFile(String message) {
        super(message);
    }
}
