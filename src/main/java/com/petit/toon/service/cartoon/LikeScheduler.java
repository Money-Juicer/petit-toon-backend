package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Dislike;
import com.petit.toon.entity.cartoon.Like;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.notfound.CartoonNotFoundException;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.cartoon.DislikeRepository;
import com.petit.toon.repository.cartoon.LikeRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Transactional
@RequiredArgsConstructor
public class LikeScheduler {
    private final UserRepository userRepository;
    private final CartoonRepository cartoonRepository;
    private final LikeRepository likeRepository;
    private final DislikeRepository dislikeRepository;
    private final RedisUtil redisUtil;

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void syncLikeAndDislike() {
        synchronize(true);
        synchronize(false);
    }

    private void synchronize(boolean isLike) {
        Set<String> keys = getKeys(isLike);
        for (String key : keys) {
            long toonId = parseKey(key);
            Set<Long> userIdsInCache = findUserIdsInCache(key);
            List<Long> userIdsInDB = findUserIdsInDB(toonId, isLike);

            deleteOldData(userIdsInCache, userIdsInDB, isLike);
            insertNewData(userIdsInCache, userIdsInDB, toonId, isLike);
        }
    }

    private Set<String> getKeys(boolean isLike) {
        if (isLike) {
            return redisUtil.getKeys(LikeService.LIKE_KEY_PREFIX + "*");
        }
        return redisUtil.getKeys(LikeService.DISLIKE_KEY_PREFIX + "*");
    }

    private long parseKey(String key) {
        return Long.parseLong(
                key.substring(key.lastIndexOf(":") + 1));
    }

    private Set<Long> findUserIdsInCache(String key) {
        return redisUtil.findIdsOfTrueBits(key);
    }

    private List<Long> findUserIdsInDB(long toonId, boolean isLike) {
        if (isLike) {
            return likeRepository.findUserIdByCartoonId(toonId);
        }
        return dislikeRepository.findUserIdByCartoonId(toonId);
    }

    private void insertNewData(Set<Long> userIdsInCache, List<Long> userIdsInDB, long toonId, boolean isLike) {
        List<Long> insertIds = userIdsInCache.stream()
                .filter(userId -> !userIdsInDB.contains(userId))
                .collect(Collectors.toList());

        List<User> insertUsers = userRepository.findAllByIdIn(insertIds);
        Cartoon cartoon = cartoonRepository.findById(toonId).
                orElseThrow(CartoonNotFoundException::new);

        if (isLike) {
            likeRepository.bulkInsert(createLikes(insertUsers, cartoon));
        } else {
            dislikeRepository.bulkInsert(createDislikes(insertUsers, cartoon));
        }
    }

    private void deleteOldData(Set<Long> userIdsInCache, List<Long> userIdsInDB, boolean isLike) {
        List<Long> deleteIds = userIdsInDB.stream()
                .filter(userId -> !userIdsInCache.contains(userId))
                .collect(Collectors.toList());

        if (isLike) {
            likeRepository.deleteAllByIdIn(deleteIds);
        } else {
            dislikeRepository.deleteAllByIdIn(deleteIds);
        }
    }

    private List<Like> createLikes(List<User> users, Cartoon cartoon) {
        return users.stream()
                .map(user -> Like.builder()
                        .user(user)
                        .cartoon(cartoon)
                        .build())
                .collect(Collectors.toList());
    }

    private List<Dislike> createDislikes(List<User> users, Cartoon cartoon) {
        return users.stream()
                .map(user -> Dislike.builder()
                        .user(user)
                        .cartoon(cartoon)
                        .build())
                .collect(Collectors.toList());
    }
}
