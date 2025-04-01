package com.example.thumbnailator.service;

import com.example.thumbnailator.cloud.StorageService;
import com.example.thumbnailator.repository.ImageRepository;
import com.example.thumbnailator.repository.ThumbnailRepository;
import org.springframework.stereotype.Service;

@Service
public class ThumbnailCreatorFactory {
    private final ThumbnailRepository thumbnailRepository;
    private final ImageRepository imageRepository;
    private final ImageProcessor imageProcessor;
    private final StorageService storageService;
    private final ThumbnailService thumbnailService;

    public ThumbnailCreatorFactory(ThumbnailRepository thumbnailRepository, ImageRepository imageRepository, ImageProcessor imageProcessor, StorageService storageService, ThumbnailService thumbnailService) {
        this.thumbnailRepository = thumbnailRepository;
        this.imageRepository = imageRepository;
        this.imageProcessor = imageProcessor;
        this.storageService = storageService;
        this.thumbnailService = thumbnailService;
    }

    public ThumbnailCreator getThumbnailCreator() {
        return new ThumbnailCreator(thumbnailRepository, imageRepository, imageProcessor, storageService, thumbnailService);
    }
}