package com.example.thumbnailator.cloud;

import java.io.InputStream;

public interface StorageService {
    String uploadFile(String originalFilename, InputStream inputStream);

    byte[] downloadFile(String fileUrl);
}
