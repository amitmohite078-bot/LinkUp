package com.linkup.repository;

import com.linkup.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByUserIdOrderByCreatedAtDesc(Long userId);
}
