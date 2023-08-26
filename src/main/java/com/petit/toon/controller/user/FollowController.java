package com.petit.toon.controller.user;

import com.petit.toon.service.user.FollowService;
import com.petit.toon.service.user.response.UserListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/api/v1/follow/{followeeId}")
    public ResponseEntity<Void> follow(@AuthenticationPrincipal(expression = "user.id") long followerId,
                                       @PathVariable("followeeId") long followeeId) {
        followService.follow(followerId, followeeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/api/v1/follow/{userId}/following")
    public ResponseEntity<UserListResponse> getFollowingUsers(@PathVariable("userId") long userId,
                                                              @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(followService.findFollowingUsers(userId, pageable));
    }

    @GetMapping("/api/v1/follow/{userId}/followed")
    public ResponseEntity<UserListResponse> getFollowedUsers(@PathVariable("userId") long userId,
                                                             @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(followService.findFollowedUsers(userId, pageable));
    }

    @DeleteMapping("/api/v1/follow/{followeeId}")
    public ResponseEntity<Void> deleteFollow(@AuthenticationPrincipal(expression = "user.id") long followerId,
                                             @PathVariable("followeeId") long followeeId) {
        followService.unfollow(followerId, followeeId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
