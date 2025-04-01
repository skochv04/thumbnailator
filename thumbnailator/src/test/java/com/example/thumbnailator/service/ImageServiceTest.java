package com.example.thumbnailator.service;

import com.example.thumbnailator.cloud.StorageService;
import com.example.thumbnailator.error.ImageForIdNotFoundException;
import com.example.thumbnailator.model.*;
import com.example.thumbnailator.repository.ImageRepository;
import com.example.thumbnailator.repository.ThumbnailRepository;
import com.example.thumbnailator.response.DetailsResponse;
import com.example.thumbnailator.response.ImagesUploadResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {
    private static final UUID THUMBNAIL_ID = UUID.randomUUID();
    private static final String URL = "url";
    private static final String FILE_NAME = "fileName";
    private static final String ORIGINAL_NAME = FILE_NAME + ".png";
    private static final Image IMAGE = new Image(ORIGINAL_NAME, URL);
    private static final String ROOT_PATH = "root";
    private static final Folder ROOT_FOLDER = new Folder(null, ROOT_PATH);
    private static final List<Thumbnail> THUMBNAILS = List.of(
            new Thumbnail(IMAGE, Size.SMALL, ROOT_FOLDER),
            new Thumbnail(IMAGE, Size.MIDDLE, ROOT_FOLDER)
    );
    private static final byte[] BYTES = "content".getBytes();
    private static final InputStream INPUT_STREAM = new ByteArrayInputStream(BYTES);
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ThumbnailRepository thumbnailRepository;
    @Mock
    private StorageService storageService;
    @Mock
    private FolderService folderService;
    @Mock
    private ThumbnailCreator thumbnailCreator;
    @Mock
    private ThumbnailCreatorFactory thumbnailCreatorFactory;
    @InjectMocks
    private ImageService imageService;
    @Captor
    private ArgumentCaptor<Thumbnail> thumbnailCaptor;

    @Test
    public void shouldSaveImageAndThumbnailsBasedOnFile() {
        // Given
        List<Thumbnail> expectedThumbnails = new ArrayList<>();
        for (Size size : Size.values()) {
            expectedThumbnails.add(new Thumbnail(IMAGE, size, ROOT_FOLDER));
        }

        // When
        when(storageService.uploadFile(ORIGINAL_NAME, INPUT_STREAM)).thenReturn(URL);
        Image result = imageService.saveFile(ORIGINAL_NAME, ROOT_PATH, INPUT_STREAM);

        // Then
        verify(imageRepository).save(IMAGE);
        verify(thumbnailRepository, times(Size.values().length)).save(thumbnailCaptor.capture());
        List<Thumbnail> capturedThumbnails = thumbnailCaptor.getAllValues();
        for (int i = 0; i < Size.values().length; i++) {
            Thumbnail expectedThumbnail = expectedThumbnails.get(i);
            Thumbnail capturedThumbnail = capturedThumbnails.get(i);
            assertEquals(expectedThumbnail.getSize(), capturedThumbnail.getSize());
            assertEquals(expectedThumbnail.getOriginal(), capturedThumbnail.getOriginal());
        }
        assertEquals(IMAGE, result);
    }

    @Test
    public void shouldGetOriginalImageByValidThumbnailId() {
        // Given
        Thumbnail thumbnail = new Thumbnail(IMAGE, Size.SMALL, ROOT_FOLDER);
        // When
        when(thumbnailRepository.findById(THUMBNAIL_ID)).thenReturn(Optional.of(thumbnail));
        Image result = imageService.getOriginalImageByThumbnailId(THUMBNAIL_ID);
        // Then
        assertEquals(IMAGE, result);
    }

    @Test
    public void shouldThrowImageForIdNotFoundExceptionFotInvalidThumbnailId() {
        // Given
        // When
        when(thumbnailRepository.findById(THUMBNAIL_ID)).thenReturn(Optional.empty());
        // Then
        assertThrows(ImageForIdNotFoundException.class,
                () -> imageService.getOriginalImageByThumbnailId(THUMBNAIL_ID),
                String.format("Image for id %s not found.", THUMBNAIL_ID));
    }

    @Test
    public void shouldReturnBatchDetailsResponse() {
        // Given
        int page = 1;
        int pageSize = 5;
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        int totalNumber = 10;
        int totalNumberPerPage = 5;
        DetailsResponse expectedResponse = new DetailsResponse(totalNumber, totalNumberPerPage, totalNumberPerPage);
        Size size = Size.SMALL;
        Thumbnail thumbnail = new Thumbnail(IMAGE, size, ROOT_FOLDER);
        List<Thumbnail> thumbnailList = new ArrayList<>(Collections.nCopies(totalNumberPerPage, thumbnail));

        // When
        when(folderService.findFolder(ROOT_PATH)).thenReturn(ROOT_FOLDER);
        when(thumbnailRepository.countDistinctByFolderAndSizeAndStatusIn(ROOT_FOLDER, size, List.of(Status.PENDING, Status.PROCESSING, Status.COMPLETED, Status.ERROR))).thenReturn(totalNumber);
        when(thumbnailRepository.findDistinctByFolderAndSizeAndStatusIn(ROOT_FOLDER, size, List.of(Status.PENDING, Status.PROCESSING, Status.COMPLETED, Status.ERROR), pageRequest)).thenReturn(thumbnailList);
        when(thumbnailRepository.findDistinctByFolderAndSizeAndStatusIn(ROOT_FOLDER, size, List.of(Status.COMPLETED), pageRequest)).thenReturn(thumbnailList);
        DetailsResponse result = imageService.getDetails(ROOT_PATH, size, page, pageSize);

        // Then
        assertEquals(expectedResponse, result);
    }

    @Test
    public void shouldUploadAllValidImages() {
        // Given
        List<String> extensions = List.of("jpg", "jpeg", "png");
        List<MultipartFile> files = testMultipartFileList(extensions);
        List<String> fileNames = files.stream().map(MultipartFile::getOriginalFilename).toList();
        ImagesUploadResponse expectedResponse = new ImagesUploadResponse(Collections.emptyList());

        // When
        when(thumbnailCreatorFactory.getThumbnailCreator()).thenReturn(thumbnailCreator);  // Return the mocked thumbnailCreator
        for (String fileName : fileNames) {
            when(storageService.uploadFile(eq(fileName), any())).thenReturn(URL);
        }
        ImagesUploadResponse response = imageService.uploadImages(files, ROOT_PATH).block();

        // Then
        for (String fileName : fileNames) {
            Image image = new Image(fileName, URL);
            for (Size size : Size.values()) {
                verify(thumbnailCreator).addTask(image, BYTES, size);
            }
        }
        verify(thumbnailCreator).run();
        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldNotUploadInvalidImages() {
        // Given
        List<String> extensions = List.of(".txt", ".ipynb", "");
        List<MultipartFile> files = testMultipartFileList(extensions);
        List<String> fileNames = files.stream().map(MultipartFile::getOriginalFilename).toList();
        ImagesUploadResponse expectedResponse = new ImagesUploadResponse(fileNames);

        // When
        when(thumbnailCreatorFactory.getThumbnailCreator()).thenReturn(thumbnailCreator);
        ImagesUploadResponse response = imageService.uploadImages(files, ROOT_PATH).block();

        // Then
        verifyNoInteractions(storageService);
        verify(thumbnailCreator).run();
        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldProcessUncompletedThumbnailsForValidBatchId() {
        // Given
        // When
        when(thumbnailCreatorFactory.getThumbnailCreator()).thenReturn(thumbnailCreator);
        when(thumbnailRepository.findThumbnailsByStatus(List.of(Status.PENDING, Status.PROCESSING, Status.ERROR)))
                .thenReturn(THUMBNAILS);
        imageService.processUncompletedThumbnails();

        // Then
        verify(thumbnailRepository).findThumbnailsByStatus(List.of(Status.PENDING, Status.PROCESSING, Status.ERROR));
        for (Thumbnail thumbnail : THUMBNAILS) {
            verify(thumbnailCreator).addUncompletedThumbnailProcessingTask(thumbnail);
        }
        verify(thumbnailCreator).run();
    }

    private List<MultipartFile> testMultipartFileList(List<String> extensions) {
        List<MultipartFile> files = new ArrayList<>();
        for (String extension : extensions) {
            files.add(new MockMultipartFile(FILE_NAME, FILE_NAME + "." + extension, null, BYTES));
        }
        return files;
    }
}
