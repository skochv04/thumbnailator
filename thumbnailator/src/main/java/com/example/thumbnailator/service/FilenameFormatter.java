package com.example.thumbnailator.service;

import org.apache.commons.io.FilenameUtils;

import java.util.UUID;

public class FilenameFormatter {

    public static String getUniqueFilename(String originalFilename) {
        String filename = getFilenameWithoutExtension(originalFilename);
        UUID uuid = UUID.randomUUID();
        String extension = getExtension(originalFilename);

        return filename + "_" + uuid + "." + extension;
    }

    public static String getFileContentType(String originalFilename) {
        String extension = getExtension(originalFilename).toLowerCase();
        if("jpg".equals(extension)){
            return "image/jpeg";
        }
        return "image/" + extension;
    }

    public static String getExtension(String originalFilename) {
        return FilenameUtils.getExtension(originalFilename);
    }

    public static String getFilenameWithoutExtension(String originalFilename) {
        return FilenameUtils.getBaseName(originalFilename);
    }
}
