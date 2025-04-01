package com.example.thumbnailator.controller;

import com.example.thumbnailator.response.ImagesUploadResponse;
import com.example.thumbnailator.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {
    private final ImageService imageService;
    private final Logger logger = LoggerFactory.getLogger(ImageController.class);

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping()
    public Mono<ResponseEntity<ImagesUploadResponse>> uploadImages(@RequestParam(value = "files") List<MultipartFile> files, @RequestParam(value = "folder") String folderPath) {
        return imageService.uploadImages(files, folderPath)
                .map(ResponseEntity::ok)
                .doOnTerminate(() -> logger.info("Images uploaded successfully"));
    }
}