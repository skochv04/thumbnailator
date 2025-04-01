package com.example.thumbnailator.service;

import com.example.thumbnailator.model.Size;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToSizeConverter implements Converter<String, Size> {
    @Override
    public Size convert(String source) {
        try {
            return Size.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid size value: " + source);
        }
    }
}