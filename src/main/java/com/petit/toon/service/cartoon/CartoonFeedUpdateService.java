package com.petit.toon.service.cartoon;

import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.petit.toon.service.feed.FeedService.FEED_KEY_PREFIX;

@Service
@Transactional
@RequiredArgsConstructor
public class CartoonFeedUpdateService {

    private final FollowRepository followRepository;
    private final RedisUtil redisUtil;

    @Async
    public void feedUpdateToFollower(long authorId, long cartoonId) {
        List<Long> followers = followRepository.findFollowerIdsByFolloweeId(authorId);
        for (long followerId : followers) {
            String key = FEED_KEY_PREFIX + followerId;
            redisUtil.pushElementWithLimit(key, cartoonId, 100);
        }
    }

}
