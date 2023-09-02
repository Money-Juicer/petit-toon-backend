package com.petit.toon.controller.feed;

import com.petit.toon.service.feed.FeedService;
import com.petit.toon.service.feed.response.FeedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/api/v1/feed")
    public ResponseEntity<FeedResponse> feedInquiry(@AuthenticationPrincipal(expression = "user.id") long userId,
                                                    @PageableDefault Pageable pageable) {

        FeedResponse response = feedService.feed(pageable, userId, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
