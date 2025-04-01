package com.example.thumbnailator.cloud;

import com.example.thumbnailator.service.FilenameFormatter;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.io.InputStream;

import com.google.auth.oauth2.ServiceAccountCredentials;

@Service
public class GoogleCloudStorageService implements StorageService {
    private Storage storage;
    private final String bucketName = "test_to_uxjava";
    private final String bucketCredentialsPath = "credentials.json";
    private final Logger logger = LoggerFactory.getLogger(GoogleCloudStorageService.class);

    public GoogleCloudStorageService() {
        try {
            this.storage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(bucketCredentialsPath)))
                    .build()
                    .getService();
        } catch (IOException e) {
            logger.error("Error creating Google Cloud Storage Service: {}", e.getMessage());
        }
    }

    public String uploadFile(String originalFilename, InputStream inputStream) {
        String uniqueFilename = FilenameFormatter.getUniqueFilename(originalFilename);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uniqueFilename).build();

        Blob blob = storage.create(blobInfo, inputStream);

        return blob.getMediaLink();
    }

    public byte[] downloadFile(String fileUrl) {
        String decodedUrl = URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(decodedUrl, byte[].class);
    }
}
