package com.petit.toon.controller.cartoon;

import com.petit.toon.service.cartoon.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/api/v1/like/{toonId}")
    public ResponseEntity<Void> like(@AuthenticationPrincipal(expression = "user.id") long userId,
                                     @PathVariable("toonId") long toonId) {
        likeService.like(userId, toonId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/dislike/{postId}")
    public ResponseEntity<Void> dislike(@AuthenticationPrincipal(expression = "user.id") long userId,
                                        @PathVariable("postId") long postId) {
        likeService.dislike(userId, postId);
        return ResponseEntity.ok().build();
    }
}
