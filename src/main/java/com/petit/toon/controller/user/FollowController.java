package com.petit.toon.controller.user;

import com.petit.toon.service.user.FollowService;
import com.petit.toon.service.user.response.FollowResponse;
import com.petit.toon.service.user.response.FollowUserListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/api/v1/follow/{followerId}/{followeeId}")
    public ResponseEntity<FollowResponse> follow(@PathVariable("followerId") long followerId,
                                                 @PathVariable("followeeId") long followeeId) {
        FollowResponse response = followService.follow(followerId, followeeId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/follow/{userId}")
    public ResponseEntity<FollowUserListResponse> getFollowingUsers(@PathVariable("userId") long userId,
                                                                    @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(followService.findFollowingUsers(userId, pageable));
    }

    @DeleteMapping("/api/v1/follow/{followId}")
    public ResponseEntity<Void> deleteFollow(@PathVariable("followId") long followId) {
        followService.unfollow(followId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
