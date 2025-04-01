package com.example.thumbnailator.service;

import com.example.thumbnailator.cloud.StorageService;
import com.example.thumbnailator.model.Image;
import com.example.thumbnailator.model.Size;
import com.example.thumbnailator.model.Status;
import com.example.thumbnailator.model.Thumbnail;
import com.example.thumbnailator.repository.ImageRepository;
import com.example.thumbnailator.repository.ThumbnailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ThumbnailCreator {
    private final ThumbnailRepository thumbnailRepository;
    private final ImageRepository imageRepository;
    private final ImageProcessor imageProcessor;
    private final StorageService storageService;
    private final List<Mono<Void>> thumbnailTasks = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(ThumbnailCreator.class);
    private final ThumbnailService thumbnailService;

    public ThumbnailCreator(ThumbnailRepository thumbnailRepository, ImageRepository imageRepository, ImageProcessor imageProcessor, StorageService storageService, ThumbnailService thumbnailService) {
        this.thumbnailRepository = thumbnailRepository;
        this.imageRepository = imageRepository;
        this.imageProcessor = imageProcessor;
        this.storageService = storageService;
        this.thumbnailService = thumbnailService;
    }

    public String createThumbnailName(Thumbnail thumbnail) {
        String thumbnailSize = thumbnail.getSize().toString().toLowerCase();
        String originalName = thumbnail.getOriginal().getName();
        return "thumbnail_" + thumbnailSize + "_" + originalName;
    }

    @Transactional
    public void processThumbnail(Image originalImage, byte[] originalBytes, Size size) {
        Thumbnail thumbnail = thumbnailRepository.findThumbnailByOriginalAndSize(originalImage, size);

        try {
            thumbnail.setStatus(Status.PROCESSING);
            thumbnailRepository.save(thumbnail);

            String thumbnailName = createThumbnailName(thumbnail);
            String thumbnailUrl = storageService.uploadFile(thumbnailName, imageProcessor.getThumbnailFromImage(originalBytes, thumbnail.getSize()));

            logger.info("Uploaded thumbnail file: {} to {}", thumbnailName, thumbnailUrl);

            Image thumbnailImage = new Image(thumbnailName, thumbnailUrl);
            imageRepository.save(thumbnailImage);

            thumbnail.setThumbnail(thumbnailImage);
            thumbnail.setStatus(Status.COMPLETED);
            thumbnailRepository.save(thumbnail);

            thumbnailService.emitReadyThumbnailToWaitingSinks(thumbnail);
            logger.info("Processed and saved {} thumbnail for image: {}", size.toString().toLowerCase(), originalImage.getName());
        } catch (IOException e) {
            logger.error("Error during thumbnail creation: {}", e.getMessage());

            thumbnail.setStatus(Status.ERROR);
            thumbnailRepository.save(thumbnail);
        }
    }

    public void addTask(Image originalImage, byte[] originalBytes, Size size) {
        Thumbnail thumbnail = thumbnailRepository.findThumbnailByOriginalAndSize(originalImage, size);

        thumbnail.setStatus(Status.PENDING);
        thumbnailRepository.save(thumbnail);

        thumbnailTasks.add(Mono.fromRunnable(() ->
                        processThumbnail(originalImage, originalBytes, size))
                .subscribeOn(Schedulers.boundedElastic())
                .then()
        );
    }

    public void addUncompletedThumbnailProcessingTask(Thumbnail thumbnail) {
        thumbnail.setStatus(Status.PENDING);
        thumbnailRepository.save(thumbnail);

        thumbnailTasks.add(Mono.fromRunnable(() -> {
                    Image originalImage = thumbnail.getOriginal();
                    byte[] imageBytes = storageService.downloadFile(originalImage.getPath());

                    processThumbnail(originalImage, imageBytes, thumbnail.getSize());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then()
        );
    }

    public void run() {
        Flux.merge(thumbnailTasks)
                .doOnComplete(() -> logger.info("All thumbnails processed"))
                .subscribe();
    }

    public int getThumbnailTasksSize() {
        return thumbnailTasks.size();
    }
}
