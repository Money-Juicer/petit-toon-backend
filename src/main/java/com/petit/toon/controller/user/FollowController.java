package com.petit.toon.controller.user;

import com.petit.toon.service.user.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
