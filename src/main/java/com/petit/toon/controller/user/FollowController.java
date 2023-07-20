package com.petit.toon.controller.user;

import com.petit.toon.service.user.FollowService;
import com.petit.toon.service.user.response.UserListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/api/v1/follow/{followerId}/{followeeId}")
    public ResponseEntity<Long> follow(@PathVariable("followerId") long followerId,
                                       @PathVariable("followeeId") long followeeId) {
        long followId = followService.follow(followerId, followeeId);
        return new ResponseEntity<>(followId, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/follow/{userId}")
    public ResponseEntity<UserListResponse> getFollowingUsers(@PathVariable("userId") long userId) {
        return ResponseEntity.ok(followService.findFollowingUsers(userId));
    }

    @DeleteMapping("/api/v1/follow/{followId}")
    public ResponseEntity<Void> deleteFollow(@PathVariable("followId") long followId) {
        followService.unfollow(followId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
