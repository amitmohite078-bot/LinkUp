package com.linkup.controller;

import com.linkup.model.Album;
import com.linkup.model.AlbumPhoto;
import com.linkup.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
@CrossOrigin(origins = "*")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @PostMapping("/create")
    public ResponseEntity<?> createAlbum(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String privacy) {
        try {
            Album album = albumService.createAlbum(currentUserId, name, description, privacy);
            return ResponseEntity.ok(album);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{albumId}/add-photo")
    public ResponseEntity<?> addPhoto(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long albumId,
            @RequestParam String photoUrl,
            @RequestParam(required = false) String caption) {
        try {
            AlbumPhoto photo = albumService.addPhotoToAlbum(currentUserId, albumId, photoUrl, caption);
            return ResponseEntity.ok(photo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Album>> getUserAlbums(@PathVariable Long userId) {
        return ResponseEntity.ok(albumService.getUserAlbums(userId));
    }

    @GetMapping("/{albumId}/photos")
    public ResponseEntity<List<AlbumPhoto>> getAlbumPhotos(@PathVariable Long albumId) {
        return ResponseEntity.ok(albumService.getAlbumPhotos(albumId));
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<?> deleteAlbum(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long albumId) {
        try {
            albumService.deleteAlbum(albumId, currentUserId);
            return ResponseEntity.ok("Album deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
