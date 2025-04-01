package com.example.thumbnailator.controller;

import com.example.thumbnailator.error.InvalidZipFile;
import com.example.thumbnailator.response.ImagesUploadResponse;
import com.example.thumbnailator.service.ZipProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/zip")
@CrossOrigin(origins = "*")
public class ZipController {
    private final Logger logger = LoggerFactory.getLogger(ZipController.class);
    private final ZipProcessor zipProcessor;

    public ZipController(ZipProcessor zipProcessor) {
        this.zipProcessor = zipProcessor;
    }

    @PostMapping()
    public ResponseEntity<ImagesUploadResponse> uploadZip(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok().body(zipProcessor.uploadZipFile(file.getInputStream()));
        } catch (InvalidZipFile | IOException e) {
            logger.error("Error while uploading zip file", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
