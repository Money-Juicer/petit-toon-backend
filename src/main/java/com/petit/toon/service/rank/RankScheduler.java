package com.petit.toon.service.rank;

import com.petit.toon.repository.rank.RankRepository;
import com.petit.toon.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.petit.toon.service.rank.RankService.CARTOON_RANK_KEY;
import static com.petit.toon.service.rank.RankService.USER_RANK_KEY;

@Component
@RequiredArgsConstructor
public class RankScheduler {
    private final RankRepository rankRepository;
    private final RedisUtil redisUtil;

    @Scheduled(cron = "0 0 0/1 * * *") // 1시간마다 실행
    public void updateRank() {
        update(USER_RANK_KEY);
        update(CARTOON_RANK_KEY);
    }

    private void update(String key) {
        if (redisUtil.hasKey(key)) {
            redisUtil.delete(key);
        }

        PageRequest pageRequest = PageRequest.of(0, 1000);
        List<Long> rank = key.equals(USER_RANK_KEY)
                ? rankRepository.findUserRank(pageRequest, LocalDateTime.now().with(LocalTime.MIN).minusDays(7))
                : rankRepository.findCartoonRank(pageRequest, LocalDateTime.now().with(LocalTime.MIN).minusDays(7));

        if (rank.isEmpty()) {
            return;
        }
        redisUtil.setList(USER_RANK_KEY, rank, 1, TimeUnit.HOURS);
    }
}
