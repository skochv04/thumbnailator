package com.example.thumbnailator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.Objects;
import java.util.UUID;

@Entity
public class Image {
    @Id @GeneratedValue private UUID id;
    private String name;
    private String path;

    public Image() {}

    public Image(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Image image)) return false;
        return Objects.equals(id, image.id)
                && Objects.equals(name, image.name)
                && Objects.equals(path, image.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, path);
    }
}
