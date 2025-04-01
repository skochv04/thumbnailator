package com.example.thumbnailator.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileValidatorTest {
    private static final String NAME = "originalName";

    @ParameterizedTest
    @ValueSource(strings = {"jpg", "jpeg", "png", "JPG", "JPEG", "PNG"})
    public void shouldReturnTrueForValidExtension(String extension){
        // Given
        String originalName = NAME+"."+extension;
        // When
        boolean result = FileValidator.isExtensionValid(originalName);
        // Then
        assertTrue(result);
    }
}
