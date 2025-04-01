package com.example.thumbnailator.service;

import com.example.thumbnailator.model.*;
import com.example.thumbnailator.repository.FolderRepository;
import com.example.thumbnailator.repository.ImageRepository;
import com.example.thumbnailator.repository.ThumbnailRepository;
import com.example.thumbnailator.response.ThumbnailResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThumbnailServiceTest {
    private static final String FOLDER_PATH = "root";
    private static final Folder FOLDER = new Folder(null, FOLDER_PATH);
    private static final Image ORIGINAL_IMAGE = new Image("name", "path");
    private static final Thumbnail THUMBNAIL = new Thumbnail(ORIGINAL_IMAGE, Size.MIDDLE, FOLDER);
    private static final int PAGE = 1;
    private static final int PAGE_SIZE = 24;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ThumbnailRepository thumbnailRepository;
    @Mock
    private FolderRepository folderRepository;
    @InjectMocks
    private ThumbnailService thumbnailService;

    @Test
    void shouldDeleteAllThumbnailSizes() {
        // Given
        List<Thumbnail> thumbnailsToDelete = List.of(THUMBNAIL);

        // When
        when(thumbnailRepository.findThumbnailsByOriginal(ORIGINAL_IMAGE)).thenReturn(thumbnailsToDelete);
        thumbnailService.deleteAllThumbnailSizes(THUMBNAIL);

        // Then
        verify(thumbnailRepository).deleteAll(thumbnailsToDelete);
        verify(imageRepository).deleteAll(anyList());
        verify(imageRepository).delete(ORIGINAL_IMAGE);
    }

    @Test
    void shouldNotDeleteThumbnailsWhenNoneArePresent() {
        // Given
        // When
        when(thumbnailRepository.findThumbnailsByOriginal(ORIGINAL_IMAGE)).thenReturn(Collections.emptyList());

        // Then
        assertFalse(thumbnailService.deleteAllThumbnailSizes(THUMBNAIL));
        verify(thumbnailRepository, never()).deleteAll(anyList());
        verify(imageRepository, never()).deleteAll(anyList());
        verify(imageRepository, never()).delete(ORIGINAL_IMAGE);
    }

    @Test
    void shouldNotDeleteThumbnailByIDWhenNoThumbnailFound() {
        // Given
        UUID thumbnailId = UUID.randomUUID();
        // When
        when(thumbnailRepository.findById(thumbnailId)).thenReturn(Optional.empty());
        boolean result = thumbnailService.deleteThumbnailByID(thumbnailId);

        // Then
        assertFalse(result);
        verify(thumbnailRepository).findById(thumbnailId);
        verifyNoMoreInteractions(thumbnailRepository, imageRepository);
    }

    @Test
    void shouldNotEmitThumbnailsWhenNoFolderFound() {
        // Given
        Sinks.Many<ThumbnailResponse> sink = Sinks.many().unicast().onBackpressureBuffer();

        // When
        when(folderRepository.findByPath(FOLDER_PATH)).thenReturn(Optional.empty());
        Mono<Void> result = thumbnailService.emitThumbnails(FOLDER_PATH, Size.SMALL, PAGE, PAGE_SIZE, sink);
        result.block();

        // Then
        verify(folderRepository).findByPath(FOLDER_PATH);
        verifyNoMoreInteractions(thumbnailRepository);
    }
}
