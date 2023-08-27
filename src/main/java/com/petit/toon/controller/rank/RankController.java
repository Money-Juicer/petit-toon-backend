package com.petit.toon.controller.rank;

import com.petit.toon.service.rank.RankService;
import com.petit.toon.service.rank.response.CartoonRankResponse;
import com.petit.toon.service.rank.response.UserRankResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class RankController {
    private final RankService rankService;

    @GetMapping("/api/v1/rank/user")
    public ResponseEntity<UserRankResponse> userRank(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(rankService.userRank(pageable, LocalDateTime.now()));
    }

    @GetMapping("/api/v1/rank/toon")
    public ResponseEntity<CartoonRankResponse> cartoonRank(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(rankService.cartoonRank(pageable, LocalDateTime.now()));
    }
}
