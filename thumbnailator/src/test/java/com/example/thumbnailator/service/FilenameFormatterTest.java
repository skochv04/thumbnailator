package com.example.thumbnailator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilenameFormatterTest {
    private static final String NAME = "originalName";
    private static final String EXTENSION = "txt";
    private static final String ORIGINAL_NAME = NAME+"."+EXTENSION;

    @Test
    public void shouldReturnUniqueFilename(){
        // Given
        // When
        String result = FilenameFormatter.getUniqueFilename(ORIGINAL_NAME);
        // Then
        String uuidPattern = "[0-9a-fA-F\\-]{36}";
        assertTrue(result.matches(NAME+"_"+uuidPattern+"\\."+EXTENSION));
    }

    @ParameterizedTest
    @CsvSource({
            "example.png, image/png",
            "example.jpg, image/jpeg",
            "example.jpeg, image/jpeg"
    })
    public void shouldReturnContentType(String originalFilename, String contentType){
        // Given
        // When
        String result = FilenameFormatter.getFileContentType(originalFilename);
        // Then
        assertEquals(result, contentType);
    }

}
