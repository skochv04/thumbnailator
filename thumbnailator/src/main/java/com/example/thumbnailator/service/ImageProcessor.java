package com.example.thumbnailator.service;
import com.example.thumbnailator.config.ThumbnailConfig;
import com.example.thumbnailator.model.Size;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ImageProcessor {

    private final ThumbnailConfig thumbnailConfig;
    public ImageProcessor(ThumbnailConfig thumbnailConfig) {
        this.thumbnailConfig = thumbnailConfig;
    }

    public InputStream getThumbnailFromImage(byte[] originalBytes, Size size) throws IOException {
        ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
        int targetWidth = size.getWidth(thumbnailConfig);
        int targetHeight = size.getHeight(thumbnailConfig);

        Thumbnails.of(new ByteArrayInputStream(originalBytes))
                .size(targetWidth, targetHeight)
                .toOutputStream(thumbnailOutputStream);

        return new ByteArrayInputStream(thumbnailOutputStream.toByteArray());
    }
}
