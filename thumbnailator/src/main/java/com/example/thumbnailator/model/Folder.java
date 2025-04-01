package com.example.thumbnailator.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Folder {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne private Folder parent;

    private String path;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> children = new ArrayList<>();

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Thumbnail> thumbnails = new ArrayList<>();

    public Folder() {}

    public Folder(Folder parent, String path) {
        this.parent = parent;
        this.path = path;
    }

    public UUID getId() { return id; }

    public Folder getParent() { return parent; }

    public String getPath() { return path; }

    public String getFolderNameFromPath() {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}
