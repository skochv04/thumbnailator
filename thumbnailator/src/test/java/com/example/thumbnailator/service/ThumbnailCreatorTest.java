package com.example.thumbnailator.service;

import com.example.thumbnailator.cloud.StorageService;
import com.example.thumbnailator.model.*;
import com.example.thumbnailator.repository.ImageRepository;
import com.example.thumbnailator.repository.ThumbnailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ThumbnailCreatorTest {
    private static final String URL = "url";
    private static final String NAME = "name";
    private static final Image IMAGE = new Image(NAME, URL);
    private static final byte[] BYTES = "some content".getBytes();
    private static final UUID THUMBNAIL_ID = UUID.randomUUID();
    private static final String ROOT_PATH = "root";
    private static final Folder ROOT_FOLDER = new Folder(null, ROOT_PATH);
    @Mock
    private ThumbnailRepository thumbnailRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ImageProcessor imageProcessor;
    @Mock
    private StorageService storageService;
    @Mock
    private ThumbnailService thumbnailService;
    @Mock
    private Thumbnail thumbnail;

    private ThumbnailCreator thumbnailCreator;

    @BeforeEach
    void setUp() {
        thumbnailCreator = new ThumbnailCreator(thumbnailRepository, imageRepository, imageProcessor, storageService, thumbnailService);
    }

    @ParameterizedTest
    @EnumSource
    public void shouldReturnThumbnailName(Size size) {
        // Given
        Thumbnail thumbnail = new Thumbnail(IMAGE, size, ROOT_FOLDER);
        String expectedResult = "thumbnail_" + size.toString().toLowerCase() + "_" + NAME;

        // When
        String result = thumbnailCreator.createThumbnailName(thumbnail);

        // Then
        assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @EnumSource
    public void shouldAddTask(Size size) {
        // Given
        when(thumbnailRepository.findThumbnailByOriginalAndSize(any(Image.class), eq(size)))
                .thenReturn(thumbnail);

        // When
        thumbnailCreator.addTask(IMAGE, BYTES, size);

        // Then
        assertEquals(1, thumbnailCreator.getThumbnailTasksSize());
    }

    @ParameterizedTest
    @EnumSource
    public void shouldAddUncompletedThumbnailProcessingTask(Size size) throws NoSuchFieldException, IllegalAccessException {
        // Given
        Thumbnail thumbnail = testThumbnail(size);

        // When
        thumbnailCreator.addUncompletedThumbnailProcessingTask(thumbnail);

        // Then
        verify(thumbnailRepository).save(thumbnail);
        assertEquals(Status.PENDING, thumbnail.getStatus());
        assertEquals(1, thumbnailCreator.getThumbnailTasksSize());
    }

    @ParameterizedTest
    @EnumSource
    public void shouldProcessThumbnailForValidImage(Size size) throws NoSuchFieldException, IllegalAccessException {
        // Given
        Thumbnail thumbnail = testThumbnail(size);
        Image thumbnailImage = new Image(thumbnailCreator.createThumbnailName(thumbnail), URL);

        // When
        when(thumbnailRepository.findThumbnailByOriginalAndSize(IMAGE, size)).thenReturn(thumbnail);
        when(storageService.uploadFile(any(), any())).thenReturn(URL);
        thumbnailCreator.processThumbnail(IMAGE, BYTES, size);

        // Then
        verify(imageRepository).save(thumbnailImage);
        verify(thumbnailRepository).findThumbnailByOriginalAndSize(IMAGE, size);
        thumbnail.setStatus(Status.PROCESSING);
        thumbnail.setStatus(Status.COMPLETED);
        thumbnail.setThumbnail(thumbnailImage);
        verify(thumbnailRepository, times(2)).save(thumbnail);
    }

    @ParameterizedTest
    @EnumSource
    public void shouldReturnEmptyOptionalOnProcessingError(Size size) throws IOException {
        // Given
        Thumbnail thumbnail = new Thumbnail(IMAGE, size, ROOT_FOLDER);

        // When
        when(thumbnailRepository.findThumbnailByOriginalAndSize(IMAGE, size)).thenReturn(thumbnail);
        when(imageProcessor.getThumbnailFromImage(BYTES, size)).thenThrow(IOException.class);
        thumbnailCreator.processThumbnail(IMAGE, BYTES, size);

        // Then
        verify(thumbnailRepository, times(2)).save(thumbnail);
        verifyNoInteractions(imageRepository);
        verify(thumbnailRepository).findThumbnailByOriginalAndSize(IMAGE, size);
        verifyNoMoreInteractions(thumbnailRepository);
    }

    private Thumbnail testThumbnail(Size size) throws NoSuchFieldException, IllegalAccessException {
        Thumbnail thumbnail = new Thumbnail(IMAGE, size, ROOT_FOLDER);
        Field field = thumbnail.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(thumbnail, THUMBNAIL_ID);
        return thumbnail;
    }
}