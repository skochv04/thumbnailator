package com.example.thumbnailator.controller;

import com.example.thumbnailator.error.ImageForIdNotFoundException;
import com.example.thumbnailator.model.Image;
import com.example.thumbnailator.model.Size;
import com.example.thumbnailator.response.DetailsResponse;
import com.example.thumbnailator.response.OriginalPhotoResponse;
import com.example.thumbnailator.response.ThumbnailResponse;
import com.example.thumbnailator.service.ImageService;
import com.example.thumbnailator.service.ThumbnailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/thumbnails")
@CrossOrigin(origins = "*")
public class ThumbnailController {
    public final int PAGE_SIZE = 24;
    private final Logger logger = LoggerFactory.getLogger(ThumbnailController.class);
    private final ImageService imageService;
    private final ThumbnailService thumbnailService;

    public ThumbnailController(ImageService imageService, ThumbnailService thumbnailService) {
        this.imageService = imageService;
        this.thumbnailService = thumbnailService;
    }

    @GetMapping(value = "/details")
    public ResponseEntity<DetailsResponse> getDetails(@RequestParam("path") String path, @RequestParam("size") Size size, @RequestParam Integer page) {
        return ResponseEntity.ok().body(imageService.getDetails(path, size, page, PAGE_SIZE));
    }

    @GetMapping(value = "/{thumbnailId}/original")
    public ResponseEntity<OriginalPhotoResponse> getOriginalImageByThumbnailId(@PathVariable UUID thumbnailId) {
        try {
            Image originalImage = imageService.getOriginalImageByThumbnailId(thumbnailId);
            return ResponseEntity.ok().body(new OriginalPhotoResponse(originalImage.getPath()));
        } catch (ImageForIdNotFoundException e) {
            logger.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ThumbnailResponse> getThumbnails(@RequestParam("path") String path, @RequestParam("size") Size size, @RequestParam Integer page) {
        logger.info("Received request for path={}, size={}, page={}", path, size, page);

        Sinks.Many<ThumbnailResponse> sink = Sinks.many().replay().all();
        thumbnailService.emitThumbnails(path, size, page, PAGE_SIZE, sink).subscribe();

        return sink
                .asFlux()
                .doOnNext(image -> logger.info("Sending processed image details: {}", image));
    }

    @PostMapping("/{thumbnailId}/move")
    public ResponseEntity<Void> moveThumbnail(
            @PathVariable UUID thumbnailId,
            @RequestBody Map<String, String> requestBody) {
        String newFolderPath = requestBody.get("newFolderPath");
        if (newFolderPath == null || newFolderPath.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            imageService.moveImageToFolder(thumbnailId, newFolderPath);
            return ResponseEntity.ok().build();
        } catch (ImageForIdNotFoundException e) {
            logger.error("Thumbnail not found: {}", thumbnailId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error moving thumbnail: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{thumbnailId}/delete")
    public ResponseEntity<Void> deleteThumbnail(@PathVariable UUID thumbnailId) {
        try {
            boolean deleted = thumbnailService.deleteThumbnailByID(thumbnailId);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting thumbnail: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}