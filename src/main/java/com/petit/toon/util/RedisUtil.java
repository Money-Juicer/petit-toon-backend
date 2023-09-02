package com.petit.toon.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    public Long setList(String key, List<Long> elements) {
        List<String> value = elements.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        redisTemplate.expire(key, DEFAULT_TIMEOUT, TimeUnit.DAYS);
        return listOperations.rightPushAll(key, value);
    }

    public Long setList(String key, List<Long> elements, long timeout, TimeUnit timeUnit) {
        List<String> value = elements.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        redisTemplate.expire(key, timeout, timeUnit);
        return listOperations.rightPushAll(key, value);
    }

    /**
     * Set Redis List with elements.
     * Just Do LeftPush in List at key, with elements.
     * Then, trim list 0 ~ (limit-1) [Size = limit]
     */
    public void setListWithLimit(String key, List<Long> elements, long limit, long timeout, TimeUnit timeUnit) {
        List<String> value = elements.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        ListOperations<String, String> listOperations = redisTemplate.opsForList();

        redisTemplate.expire(key, timeout, timeUnit);
        listOperations.leftPushAll(key, value);

        // Redis List Size Limit
        listOperations.trim(key, 0, limit - 1);
//        return listOperations.size(key);
    }

    public void pushElementWithLimit(String key, long element, long limit) {
        String value = String.valueOf(element);
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) {
                long listSize = redisOperations.opsForList().size(key);
                redisOperations.watch(key);
                redisOperations.multi();

                if (listSize == limit) {
                    redisOperations.opsForList().rightPop(key);
                }
                redisOperations.opsForList().leftPush(key, value);

                return redisOperations.exec();
            }
        });
//        ListOperations<String, String> listOperations = redisTemplate.opsForList();
//
//
//        listOperations.getOperations().watch(key);
//        listOperations.getOperations().multi();
//        if (listOperations.size(key) == limit) {
//            listOperations.rightPop(key);
//        }
//        listOperations.leftPush(key, value);
    }


    public List<Long> getList(String key, long start, long end) {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        List<String> elements = listOperations.range(key, start, end - 1);
        return elements == null ? List.of() : elements.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public List<Long> getList(String key, long start, long end, long timeout, TimeUnit timeUnit) {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        List<String> elements = listOperations.range(key, start, end - 1);
        redisTemplate.expire(key, timeout, timeUnit);
        return elements == null ? List.of() : elements.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public void flushAll() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();
    }
}
