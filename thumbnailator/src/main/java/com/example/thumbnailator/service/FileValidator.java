package com.example.thumbnailator.service;

import java.util.List;

public class FileValidator {
    public static final List<String> VALID_EXTENSIONS = List.of("jpg", "jpeg", "png");

    public static boolean isExtensionValid(String originalFilename){
        return VALID_EXTENSIONS.contains(FilenameFormatter.getExtension(originalFilename).toLowerCase());
    }
}
