package com.example.thumbnailator.response;

import java.util.List;

public record ImagesUploadResponse(List<String> invalidFiles) {
}
