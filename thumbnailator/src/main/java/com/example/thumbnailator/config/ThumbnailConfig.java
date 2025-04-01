package com.example.thumbnailator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "thumbnail")
public class ThumbnailConfig {
    private int smallWidth;
    private int smallHeight;
    private int middleWidth;
    private int middleHeight;
    private int bigWidth;
    private int bigHeight;

    public int getSmallWidth() {
        return smallWidth;
    }

    public void setSmallWidth(int smallWidth) {
        this.smallWidth = smallWidth;
    }

    public int getSmallHeight() {
        return smallHeight;
    }

    public void setSmallHeight(int smallHeight) {
        this.smallHeight = smallHeight;
    }

    public int getMiddleWidth() {
        return middleWidth;
    }

    public void setMiddleWidth(int middleWidth) {
        this.middleWidth = middleWidth;
    }

    public int getMiddleHeight() {
        return middleHeight;
    }

    public void setMiddleHeight(int middleHeight) {
        this.middleHeight = middleHeight;
    }

    public int getBigWidth() {
        return bigWidth;
    }

    public void setBigWidth(int bigWidth) {
        this.bigWidth = bigWidth;
    }

    public int getBigHeight() {
        return bigHeight;
    }

    public void setBigHeight(int bigHeight) {
        this.bigHeight = bigHeight;
    }
}