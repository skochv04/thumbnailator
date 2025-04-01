package com.example.thumbnailator.service;

import com.example.thumbnailator.cloud.StorageService;
import com.example.thumbnailator.error.ImageForIdNotFoundException;
import com.example.thumbnailator.model.*;
import com.example.thumbnailator.repository.ImageRepository;
import com.example.thumbnailator.repository.ThumbnailRepository;
import com.example.thumbnailator.response.DetailsResponse;
import com.example.thumbnailator.response.ImagesUploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final ThumbnailRepository thumbnailRepository;
    private final StorageService storageService;
    private final FolderService folderService;
    private final ThumbnailCreatorFactory thumbnailCreatorFactory;
    private final Logger logger = LoggerFactory.getLogger(ImageService.class);

    public ImageService(ImageRepository imageRepository, ThumbnailRepository thumbnailRepository, StorageService storageService, ThumbnailCreatorFactory thumbnailCreatorFactory, FolderService folderService) {
        this.imageRepository = imageRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.storageService = storageService;
        this.thumbnailCreatorFactory = thumbnailCreatorFactory;
        this.folderService = folderService;
    }

    @Transactional
    public Image saveFile(String name, String folderPath, InputStream inputStream) {
        String imageUrl = storageService.uploadFile(name, inputStream);
        logger.info("Uploaded file: {} to {}", name, imageUrl);

        Image originalImage = new Image(name, imageUrl);
        imageRepository.save(originalImage);

        Folder folder = folderService.findFolder(folderPath);

        for (Size size : Size.values()) {
            Thumbnail thumbnail = new Thumbnail(originalImage, size, folder);
            thumbnailRepository.save(thumbnail);
        }

        return originalImage;
    }

    @Transactional
    public Mono<ImagesUploadResponse> uploadImages(List<? extends MultipartFile> files, String folderPath) {
        ThumbnailCreator thumbnailCreator = thumbnailCreatorFactory.getThumbnailCreator();

        List<String> invalidFiles = files.stream()
                .map(MultipartFile::getOriginalFilename)
                .filter(originalFilename -> !FileValidator.isExtensionValid(originalFilename))
                .toList();

        return Flux.fromIterable(files)
                .filter(file -> FileValidator.isExtensionValid(file.getOriginalFilename()))
                .publishOn(Schedulers.boundedElastic())
                .flatMap(file -> processFile(file, folderPath, thumbnailCreator))
                .then() // after all original files were saved
                .doFinally(signalType -> {
                    // start processing thumbnails in background
                    thumbnailCreator.run();
                })
                .thenReturn(new ImagesUploadResponse(invalidFiles));
    }

    private Mono<Image> processFile(MultipartFile file, String folderPath, ThumbnailCreator thumbnailCreator) {
        return Mono.fromCallable(() -> {
            Image originalImage = saveFile(file.getOriginalFilename(), folderPath, file.getInputStream());
            for (Size size : Size.values()) {
                thumbnailCreator.addTask(originalImage, file.getBytes(), size);
            }
            return originalImage;
        });
    }

    @Transactional(readOnly = true)
    public void processUncompletedThumbnails() {
        ThumbnailCreator thumbnailCreator = thumbnailCreatorFactory.getThumbnailCreator();
        List<Thumbnail> uncompletedThumbnails = thumbnailRepository.findThumbnailsByStatus(List.of(Status.PENDING, Status.PROCESSING, Status.ERROR));

        for (Thumbnail thumbnail : uncompletedThumbnails) {
            thumbnailCreator.addUncompletedThumbnailProcessingTask(thumbnail);
        }

        thumbnailCreator.run();
    }

    @Transactional(readOnly = true)
    public Image getOriginalImageByThumbnailId(UUID thumbnailId) throws ImageForIdNotFoundException {
        Thumbnail thumbnail = thumbnailRepository.findById(thumbnailId).orElse(null);
        if (thumbnail == null) {
            throw new ImageForIdNotFoundException(String.format("Image for id %s not found.", thumbnailId));
        }

        return thumbnail.getOriginal();
    }

    @Transactional(readOnly = true)
    public DetailsResponse getDetails(String path, Size size, int page, int pageSize) {
        Folder folder = folderService.findFolder(path);
        PageRequest pageRequest = PageRequest.of(page, pageSize);

        int totalNumberOfImages = thumbnailRepository.countDistinctByFolderAndSizeAndStatusIn(folder, size, Arrays.asList(Status.values()));
        int totalNumberOfImagesPerPage = thumbnailRepository.findDistinctByFolderAndSizeAndStatusIn(folder, size, Arrays.asList(Status.values()), pageRequest).size();
        int numberOfProcessedImagesPerPage = thumbnailRepository.findDistinctByFolderAndSizeAndStatusIn(folder, size, List.of(Status.COMPLETED), pageRequest).size();

        return new DetailsResponse(totalNumberOfImages, totalNumberOfImagesPerPage, numberOfProcessedImagesPerPage);
    }

    @Transactional
    public void moveImageToFolder(UUID thumbnailId, String newFolderPath) throws ImageForIdNotFoundException {
        Thumbnail thumbnail = thumbnailRepository.findById(thumbnailId)
                .orElseThrow(() -> new ImageForIdNotFoundException("Thumbnail not found: " + thumbnailId));
        List<Thumbnail> thumbnailsToMove = thumbnailRepository.findThumbnailsByOriginal(thumbnail.getOriginal());
        Folder folder = folderService.findFolder(newFolderPath);
        for (Thumbnail thumbnailToMove : thumbnailsToMove) {
            thumbnailToMove.setFolder(folder);
            thumbnailRepository.save(thumbnailToMove);
        }
    }
}