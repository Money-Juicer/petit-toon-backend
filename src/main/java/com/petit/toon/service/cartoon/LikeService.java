package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.LikeStatus;
import com.petit.toon.repository.cartoon.DislikeRepository;
import com.petit.toon.repository.cartoon.LikeRepository;
import com.petit.toon.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeService {
    public static final String LIKE_KEY_PREFIX = "like:toon:"; // like:toon:{toonId}
    public static final String DISLIKE_KEY_PREFIX = "dislike:toon:"; // dislike:toon:{toonId}

    private final LikeRepository likeRepository;
    private final DislikeRepository dislikeRepository;
    private final RedisUtil redisUtil;

    private record EmotionKeys(String likeKey, String dislikeKey) {
    }

    public void like(long userId, long toonId) {
        EmotionKeys keys = generateKeys(toonId);
        loadDataUnlessCached(keys, toonId);

        if (checkDataExists(keys.likeKey, userId)) {
            removeData(keys.likeKey, userId);
            return;
        }
        if (checkDataExists(keys.dislikeKey, userId)) {
            removeData(keys.dislikeKey, userId);
        }
        addData(keys.likeKey, userId);
    }

    public void dislike(long userId, long toonId) {
        EmotionKeys keys = generateKeys(toonId);
        loadDataUnlessCached(keys, toonId);

        if (checkDataExists(keys.dislikeKey, userId)) {
            removeData(keys.dislikeKey, userId);
            return;
        }
        if (checkDataExists(keys.likeKey, userId)) {
            removeData(keys.likeKey, userId);
        }
        addData(keys.dislikeKey, userId);
    }

    public long count(long toonId, boolean isLike) {
        String key = isLike ? LIKE_KEY_PREFIX + toonId : DISLIKE_KEY_PREFIX + toonId;
        if (redisUtil.hasKey(key)) {
            return redisUtil.countBits(key);
        }

        if (isLike) {
            loadLikesFromDB(key, toonId);
        } else {
            loadDislikesFromDB(key, toonId);
        }
        return redisUtil.countBits(key);
    }

    public LikeStatus isLiked(long userId, long toonId) {
        EmotionKeys keys = generateKeys(toonId);
        loadDataUnlessCached(keys, toonId);

        if (redisUtil.getBit(keys.likeKey, userId)) {
            return LikeStatus.LIKE;
        }
        if (redisUtil.getBit(keys.dislikeKey, userId)) {
            return LikeStatus.DISLIKE;
        }
        return LikeStatus.NONE;
    }

    private EmotionKeys generateKeys(long toonId) {
        String likeKey = LIKE_KEY_PREFIX + toonId;
        String dislikeKey = DISLIKE_KEY_PREFIX + toonId;
        return new EmotionKeys(likeKey, dislikeKey);
    }

    private void loadDataUnlessCached(EmotionKeys keys, long toonId) {
        if (!redisUtil.hasKey(keys.likeKey)) {
            loadLikesFromDB(keys.likeKey, toonId);
        }
        if (!redisUtil.hasKey(keys.dislikeKey)) {
            loadDislikesFromDB(keys.dislikeKey, toonId);
        }
    }

    private boolean checkDataExists(String key, long userId) {
        return redisUtil.getBit(key, userId);
    }

    private void addData(String key, long userId) {
        redisUtil.setBit(key, userId, true);
    }

    private void removeData(String key, long userId) {
        redisUtil.setBit(key, userId, false);
    }

    private void loadLikesFromDB(String key, long toonId) {
        likeRepository.findUserIdByCartoonId(toonId)
                .forEach(userId -> redisUtil.setBit(key, userId, true));
    }

    private void loadDislikesFromDB(String key, long toonId) {
        dislikeRepository.findUserIdByCartoonId(toonId)
                .forEach(userId -> redisUtil.setBit(key, userId, true));
    }
}
