package com.example.thumbnailator.repository;

import com.example.thumbnailator.model.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ThumbnailRepository extends JpaRepository<Thumbnail, UUID> {

    List<Thumbnail> findDistinctByFolderAndSizeAndStatusIn(Folder folder, Size size, List<Status> statusList, Pageable pageable);

    List<Thumbnail> findThumbnailsByFolderAndSizeAndStatusIn(Folder folder, Size size, List<Status> statusList, Pageable pageable);

    Integer countDistinctByFolderAndSizeAndStatusIn(Folder folder, Size size, List<Status> statusList);

    @Query("SELECT t FROM Thumbnail t WHERE t.original = :original AND t.size = :size")
    Thumbnail findThumbnailByOriginalAndSize(Image original, Size size);

    @Query("SELECT t FROM Thumbnail t WHERE t.status IN :status")
    List<Thumbnail> findThumbnailsByStatus(List<Status> status);

    @Query("SELECT t FROM Thumbnail t WHERE t.original = :original")
    List<Thumbnail> findThumbnailsByOriginal(Image original);

    @Query("SELECT t FROM Thumbnail t WHERE t.folder = :folder")
    List<Thumbnail> findThumbnailsByFolder(Folder folder);
}
