package com.example.thumbnailator.repository;

import com.example.thumbnailator.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
    Optional<Folder> findByPath(String path);

    @Query("SELECT f FROM Folder f WHERE f.parent = :parent")
    List<Folder> findFoldersByParent(Folder parent);
}