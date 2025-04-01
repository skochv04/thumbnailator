package com.example.thumbnailator.controller;

import com.example.thumbnailator.error.ImageForIdNotFoundException;
import com.example.thumbnailator.model.Image;
import com.example.thumbnailator.model.Size;
import com.example.thumbnailator.response.DetailsResponse;
import com.example.thumbnailator.service.ImageService;
import com.example.thumbnailator.service.ThumbnailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ThumbnailController.class)
public class ThumbnailControllerTest {
    private static final UUID THUMBNAIL_ID = UUID.randomUUID();
    private static final String URI_PREFIX = "/api/thumbnails";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String FOLDER_PATH = "/xyz/abc";
    private static final Size SIZE = Size.MIDDLE;
    private static final int PAGE = 1;
    private static final int PAGE_SIZE = 24;

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ImageService imageService;
    @MockitoBean
    private ThumbnailService thumbnailService;

    @Test
    public void shouldReturnCorrectImageUrlForValidThumbnailId() throws Exception {
        // Given
        String path = "/some/path";
        Image image = new Image("name", path);
        // When
        when(imageService.getOriginalImageByThumbnailId(THUMBNAIL_ID)).thenReturn(image);
        // Then
        this.mockMvc.perform(get(URI_PREFIX + "/{thumbnailId}/original", THUMBNAIL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(path));
    }

    @Test
    public void shouldReturnNotFoundForInvalidThumbnailId() throws Exception {
        // Given
        String errorMsg = "error";
        // When
        when(imageService.getOriginalImageByThumbnailId(THUMBNAIL_ID)).thenThrow(new ImageForIdNotFoundException(errorMsg));
        // Then
        this.mockMvc.perform(get(URI_PREFIX + "/{thumbnailId}/original", THUMBNAIL_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnNumberOfImages() throws Exception {
        // Given
        DetailsResponse detailsResponse = new DetailsResponse(50, 10, 3);
        // When
        when(imageService.getDetails(FOLDER_PATH, SIZE, PAGE, PAGE_SIZE)).thenReturn(detailsResponse);
        // Then
        this.mockMvc.perform(get(URI_PREFIX + "/details")
                        .param("path", FOLDER_PATH)
                        .param("size", SIZE.toString())
                        .param("page", String.valueOf(PAGE)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(detailsResponse)));
    }
}