package com.petit.toon.service.feed;


import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.rank.RankRepository;
import com.petit.toon.service.cartoon.LikeService;
import com.petit.toon.service.feed.response.FeedResponse;
import com.petit.toon.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {
    public static final String FEED_KEY_PREFIX = "feed:";

    private final RankRepository rankRepository;
    private final CartoonRepository cartoonRepository;
    private final LikeService likeService;
    private final RedisUtil redisUtil;

    public FeedResponse feed(Pageable pageable, long userId, LocalDateTime dateTime) {
        List<Cartoon> cachedCartoons = cartoonRepository.findAllWithExactOrder(
                redisUtil.getList(FEED_KEY_PREFIX + userId,
                        pageable.getPageNumber() * pageable.getPageSize(),
                        (pageable.getPageNumber() + 1) * pageable.getPageSize(),
                        7, TimeUnit.DAYS));
        List<Cartoon> influencerCartoons = cartoonRepository.findAllWithFollower(userId, pageable);
        List<Cartoon> rankCartoons = cartoonRepository.findAllWithExactOrder(
                rankRepository.findCartoonRank(pageable, dateTime.with(LocalTime.MIN).minusDays(7)));

        List<Long> cartoonIds = Stream.of(cachedCartoons, influencerCartoons, rankCartoons) // List stream
                .flatMap(Collection::stream)
                .distinct()
                .sorted(Comparator.comparing(this::calculateScore).reversed())
                .map(Cartoon::getId)
                .collect(Collectors.toList());

        return new FeedResponse(cartoonIds);
    }

    public double calculateScore(Cartoon cartoon) {
        long likeScore = likeService.count(cartoon.getId(), true) - likeService.count(cartoon.getId(), false);
        double sign = Math.signum(likeScore);
        return (Math.log10(Math.max(Math.abs(likeScore), 1)) * sign)
                + ((cartoon.getCreatedDateTime().toEpochSecond(ZoneOffset.UTC) - 1689433200) / 45000);
    }
}
