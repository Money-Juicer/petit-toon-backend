package com.petit.toon.repository.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    public static final int DEFAULT_TIMEOUT = 2;

    private final StringRedisTemplate redisTemplate;

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Set<String> getKeys(String key) {
        return redisTemplate.keys(key + "*");
    }

    public boolean setBit(String key, long id, boolean value) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Boolean result = valueOperations.setBit(key, id, value);
        redisTemplate.expire(key, DEFAULT_TIMEOUT, TimeUnit.DAYS);
        return result;
    }

    public boolean setBit(String key, long id, boolean value, long timeout, TimeUnit timeUnit) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Boolean result = valueOperations.setBit(key, id, value);
        redisTemplate.expire(key, timeout, timeUnit);
        return result;
    }

    public boolean getBit(String key, long id) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Boolean result = valueOperations.getBit(key, id);
        redisTemplate.expire(key, DEFAULT_TIMEOUT, TimeUnit.DAYS);
        return result;
    }

    public boolean getBit(String key, long id, long timeout, TimeUnit timeUnit) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Boolean result = valueOperations.getBit(key, id);
        redisTemplate.expire(key, timeout, timeUnit);
        return result;
    }

    public Set<Long> findIdsOfTrueBits(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        byte[] bytes = redisTemplate.getStringSerializer()
                .serialize(valueOperations.get(key));

        Set<Long> result = new HashSet<>();
        for (int i = 0; i < bytes.length; i++) {
            byte value = bytes[i];
            for (int j = 0; j < 8; j++) {
                if ((value & (1 << (7 - j))) != 0) {
                    result.add((long) i * 8 + j);
                }
            }
        }
        return result;
    }

    public Long countBits(String key) {
        Long result = redisTemplate.execute(
                (RedisCallback<Long>) connection -> connection
                        .stringCommands()
                        .bitCount(key.getBytes()));
        redisTemplate.expire(key, DEFAULT_TIMEOUT, TimeUnit.DAYS);
        return result;
    }

    public void flushAll() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();
    }
}
