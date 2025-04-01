package com.example.thumbnailator.service;

import com.example.thumbnailator.error.InvalidZipFile;
import com.example.thumbnailator.response.ImagesUploadResponse;
import com.example.thumbnailator.util.MultipartImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ZipProcessor {
    private final Logger logger = LoggerFactory.getLogger(ZipProcessor.class);
    private final ImageService imageService;
    private final FolderService folderService;

    public ZipProcessor(ImageService imageService, FolderService folderService) {
        this.imageService = imageService;
        this.folderService = folderService;
    }

    private byte[] getImageBytes(ZipInputStream zipInputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    private String getFolderPathFromName(String name) {
        int idx = name.lastIndexOf('/');
        return name.substring(0, idx);
    }

    private String getImageOriginalFilenameFromName(String name) {
        int length = name.length();
        int idx = name.lastIndexOf('/');
        if(idx == -1){
            return name.substring(0, length);
        }
        return name.substring(idx, length);
    }

    private ImagesUploadResponse uploadImagesToSpecificFolders(Map<String, List<MultipartImage>> mapFolderPathToImages) {
        List<String> invalidFiles = new LinkedList<>();
        for (String path : mapFolderPathToImages.keySet()) {
            List<MultipartImage> images = mapFolderPathToImages.get(path);
            logger.info("Uploading {} images for path: {}", images.size(), path);
            ImagesUploadResponse response = imageService.uploadImages(images, path).block();
            if (response != null) {
                invalidFiles.addAll(response.invalidFiles().stream().map(fileName -> path + "/" + fileName).toList());
            } else {
                logger.warn("Upload images response is null for path: {}", path);
            }
        }
        return new ImagesUploadResponse(invalidFiles);
    }

    public ImagesUploadResponse uploadZipFile(InputStream inputStream) {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        Map<String, List<MultipartImage>> map = new HashMap<>();
        ZipEntry zipEntry;
        try {
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory() || zipEntry.getName().endsWith("/")) {
                    String folderPath = getFolderPathFromName(zipEntry.getName());
                    folderService.createFolderAndAllParentFolders(folderPath);
                } else {
                    String name = zipEntry.getName();
                    MultipartImage multipartImage = new MultipartImage(getImageOriginalFilenameFromName(name), getImageBytes(zipInputStream));
                    String folderPath = getFolderPathFromName(name);
                    folderService.createFolderAndAllParentFolders(folderPath);
                    map.computeIfAbsent(folderPath, k -> new LinkedList<>()).add(multipartImage);
                }
                zipInputStream.closeEntry();
            }
            logger.info("Zip processed successfully.");
            return uploadImagesToSpecificFolders(map);
        } catch (IOException e) {
            throw new InvalidZipFile("Encountered an error during zip precessing: " + e.getMessage());
        }
    }
}
