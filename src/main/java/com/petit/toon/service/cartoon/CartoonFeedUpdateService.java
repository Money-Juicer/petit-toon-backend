package com.petit.toon.service.cartoon;

import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.service.cartoon.event.CartoonUploadedEvent;
import com.petit.toon.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.petit.toon.service.feed.FeedService.FEED_KEY_PREFIX;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CartoonFeedUpdateService {

    private final FollowRepository followRepository;
    private final RedisUtil redisUtil;

    @EventListener
    public void feedUpdateToFollower(CartoonUploadedEvent event) {
        log.info("Cartoon Uploaded. UserId {} starts feed update.", event.getAuthorId());
        List<Long> followers = followRepository.findFollowerIdsByFolloweeId(event.getAuthorId());
        for (long followerId : followers) {
            String key = FEED_KEY_PREFIX + followerId;
            pushToUserFeed(key, event.getCartoonId());
        }
        log.info("UserId {} ends feed update.", event.getAuthorId());
    }

    @Async
    protected void pushToUserFeed(String key, long value) {
        redisUtil.pushElementWithLimit(key, value, 100);
    }

}
