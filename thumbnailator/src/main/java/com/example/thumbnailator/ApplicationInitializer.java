package com.example.thumbnailator;

import com.example.thumbnailator.service.FolderService;
import com.example.thumbnailator.service.ImageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInitializer {
    private final ImageService imageService;
    private final FolderService folderService;
    private final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);

    public ApplicationInitializer(ImageService imageService, FolderService folderService) {
        this.imageService = imageService;
        this.folderService = folderService;
    }

    @PostConstruct
    public void initialize() {
        imageService.processUncompletedThumbnails();
        folderService.createRootFolder();
        logger.info("Initialized application successfully.");
    }
}