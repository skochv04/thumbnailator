package com.example.thumbnailator.service;

import com.example.thumbnailator.model.*;
import com.example.thumbnailator.repository.FolderRepository;
import com.example.thumbnailator.repository.ImageRepository;
import com.example.thumbnailator.repository.ThumbnailRepository;
import com.example.thumbnailator.response.ThumbnailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class ThumbnailService {
    private final ImageRepository imageRepository;
    private final ThumbnailRepository thumbnailRepository;
    private final Logger logger = LoggerFactory.getLogger(ThumbnailService.class);
    private final FolderRepository folderRepository;
    private final ConcurrentMap<UUID, List<Sinks.Many<ThumbnailResponse>>> sinksWaitingForThumbnail = new ConcurrentHashMap<>();

    public ThumbnailService(ImageRepository imageRepository, ThumbnailRepository thumbnailRepository, FolderRepository folderRepository) {
        this.imageRepository = imageRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.folderRepository = folderRepository;
    }

    @Transactional
    public boolean deleteAllThumbnailSizes(Thumbnail thumbnail) {
        Image originalImage = thumbnail.getOriginal();
        List<Thumbnail> thumbnailsToDelete = thumbnailRepository.findThumbnailsByOriginal(originalImage);

        if (thumbnailsToDelete.isEmpty()) {
            return false;
        }

        thumbnailRepository.deleteAll(thumbnailsToDelete);

        List<Image> thumbnailImages = thumbnailsToDelete.stream()
                .map(Thumbnail::getThumbnail)
                .collect(Collectors.toList());
        imageRepository.deleteAll(thumbnailImages);

        imageRepository.delete(originalImage);

        return true;
    }

    @Transactional(readOnly = true)
    public boolean deleteThumbnailByID(UUID thumbnailId) {
        Optional<Thumbnail> optionalThumbnail = thumbnailRepository.findById(thumbnailId);
        if (optionalThumbnail.isEmpty()) {
            logger.warn("Thumbnail not found for ID: {}", thumbnailId);
            return false;
        }
        return deleteAllThumbnailSizes(optionalThumbnail.get());
    }

    public Mono<Void> emitThumbnails(String path, Size size, int page, int pageSize, Sinks.Many<ThumbnailResponse> sink) {
        PageRequest pageRequest = PageRequest.of(page, pageSize);

        return Mono.fromCallable(() -> folderRepository.findByPath(path))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalFolder -> {
                    if (optionalFolder.isEmpty()) {
                        return Mono.empty(); // Jeśli folder nie istnieje, zakończ strumień
                    }
                    Folder folder = optionalFolder.get();

                    return Mono.defer(() -> {
                        List<Thumbnail> completedThumbnails = thumbnailRepository.findThumbnailsByFolderAndSizeAndStatusIn(
                                folder, size, List.of(Status.COMPLETED), pageRequest);

                        List<Thumbnail> uncompletedThumbnails = thumbnailRepository.findThumbnailsByFolderAndSizeAndStatusIn(
                                folder, size, List.of(Status.PENDING, Status.PROCESSING, Status.ERROR), pageRequest);

                        return Flux.fromIterable(completedThumbnails)
                                .doOnNext(thumbnail -> {
                                    ThumbnailResponse response = new ThumbnailResponse(
                                            thumbnail.getId(),
                                            thumbnail.getThumbnail().getPath(),
                                            size,
                                            path
                                    );
                                    sink.tryEmitNext(response);
                                })
                                .thenMany(Flux.fromIterable(uncompletedThumbnails)
                                        .doOnNext(thumbnail -> {
                                            sinksWaitingForThumbnail
                                                    .computeIfAbsent(thumbnail.getId(), k -> new ArrayList<>())
                                                    .add(sink);
                                        })
                                )
                                .then(); // Zamknij Mono po zakończeniu
                    });
                });
    }

    public void emitReadyThumbnailToWaitingSinks(Thumbnail thumbnail) {
        UUID thumbnailId = thumbnail.getId();
        ThumbnailResponse response = new ThumbnailResponse(thumbnailId, thumbnail.getThumbnail().getPath(), thumbnail.getSize(), thumbnail.getFolder().getPath());

        if (!sinksWaitingForThumbnail.containsKey(thumbnail.getId())) {
            return;
        }

        for (Sinks.Many<ThumbnailResponse> sink : sinksWaitingForThumbnail.get(thumbnailId)) {
            sink.tryEmitNext(response);
        }

        sinksWaitingForThumbnail.remove(thumbnailId);
    }
}
