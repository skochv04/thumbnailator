package com.example.thumbnailator.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Thumbnail {
    @Id @GeneratedValue private UUID id;
    @ManyToOne private Image original;
    @OneToOne private Image thumbnail;
    @Enumerated(EnumType.STRING) private Size size;
    @Enumerated(EnumType.STRING) private Status status;
    @ManyToOne private Folder folder;

    public Thumbnail(){ }

    public Thumbnail(Image original, Size size, Folder folder) {
        this.original = original;
        this.thumbnail = null;
        this.status = Status.PENDING;
        this.size = size;
        this.folder = folder;
    }

    public UUID getId() {
        return id;
    }

    public Image getOriginal() {
        return original;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Size getSize() {
        return size;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }
}
