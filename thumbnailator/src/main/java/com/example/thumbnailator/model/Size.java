package com.example.thumbnailator.model;

import com.example.thumbnailator.config.ThumbnailConfig;

public enum Size {
    SMALL,
    MIDDLE,
    BIG;

    public int getHeight(ThumbnailConfig config) {
        return switch (this) {
            case SMALL -> config.getSmallHeight();
            case MIDDLE -> config.getMiddleHeight();
            case BIG -> config.getBigHeight();
        };
    }

    public int getWidth(ThumbnailConfig config) {
        return switch (this) {
            case SMALL -> config.getSmallWidth();
            case MIDDLE -> config.getMiddleWidth();
            case BIG -> config.getBigWidth();
        };
    }
}