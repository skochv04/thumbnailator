package com.example.thumbnailator.response;

import com.example.thumbnailator.model.Size;

import java.util.UUID;

public record ThumbnailResponse(UUID id, String imageUrl, Size size, String path) {
}