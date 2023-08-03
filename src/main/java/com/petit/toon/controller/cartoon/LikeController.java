package com.petit.toon.controller.cartoon;

import com.petit.toon.service.cartoon.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/api/v1/like/{userId}/{toonId}")
    public ResponseEntity<Void> like(@PathVariable("userId") long userId,
                                     @PathVariable("toonId") long toonId) {
        likeService.like(userId, toonId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/dislike/{userId}/{postId}")
    public ResponseEntity<Void> dislike(@PathVariable("userId") long userId,
                                        @PathVariable("postId") long postId) {
        likeService.dislike(userId, postId);
        return ResponseEntity.ok().build();
    }
}
