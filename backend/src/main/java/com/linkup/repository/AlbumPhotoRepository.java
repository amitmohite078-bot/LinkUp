package com.linkup.repository;

import com.linkup.model.AlbumPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumPhotoRepository extends JpaRepository<AlbumPhoto, Long> {
    List<AlbumPhoto> findByAlbumIdOrderByCreatedAtDesc(Long albumId);
}
