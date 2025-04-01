package com.example.thumbnailator.repository;

import com.example.thumbnailator.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {
    @Query("SELECT i FROM Image i " +
            "WHERE NOT EXISTS (" +
            "    SELECT t FROM Thumbnail t " +
            "    WHERE t.original = i OR t.thumbnail = i" +
            ")")
    List<Image> findOrphanImages();
}
