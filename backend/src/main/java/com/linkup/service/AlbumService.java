package com.linkup.service;

import com.linkup.model.Album;
import com.linkup.model.AlbumPhoto;
import com.linkup.model.User;
import com.linkup.repository.AlbumPhotoRepository;
import com.linkup.repository.AlbumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private AlbumPhotoRepository albumPhotoRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Album createAlbum(Long userId, String name, String description, String privacy) {
        User user = userService.getById(userId);

        Album album = Album.builder()
                .user(user)
                .name(name)
                .description(description)
                .privacy(privacy != null ? privacy : "PUBLIC")
                .build();

        return albumRepository.save(album);
    }

    @Transactional
    public AlbumPhoto addPhotoToAlbum(Long userId, Long albumId, String photoUrl, String caption) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        if (!album.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You do not own this album");
        }

        AlbumPhoto photo = AlbumPhoto.builder()
                .album(album)
                .photoUrl(photoUrl)
                .caption(caption)
                .build();

        return albumPhotoRepository.save(photo);
    }

    public List<Album> getUserAlbums(Long userId) {
        return albumRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<AlbumPhoto> getAlbumPhotos(Long albumId) {
        return albumPhotoRepository.findByAlbumIdOrderByCreatedAtDesc(albumId);
    }

    @Transactional
    public void deleteAlbum(Long albumId, Long userId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        if (!album.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You do not own this album");
        }

        // JPA cascades or manual deletion of child photos can be configured, or manual delete
        albumRepository.delete(album);
    }
}
