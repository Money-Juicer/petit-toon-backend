package com.petit.toon.repository.util;

import com.petit.toon.util.RedisUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RedisUtilTest {

    @Autowired
    RedisUtil redisUtil;

    @AfterEach
    void tearDown() {
        redisUtil.flushAll();
    }

    @Test
    @DisplayName("메소드를 호출하면 해당 key에 timeout이 설정된다")
    void timeout() throws InterruptedException {
        // given
        String key = "feed:user:1";

        // when
        redisUtil.setBit(key, 1, true, 1, TimeUnit.SECONDS);

        // then
        MILLISECONDS.sleep(500);
        assertThat(redisUtil.hasKey(key)).isTrue();
        MILLISECONDS.sleep(500);
        assertThat(redisUtil.hasKey(key)).isFalse();
    }

    @Test
    @DisplayName("key가 존재할 때, 메소드를 호출하면 timeout 시간이 재설정된다.")
    void timeout2() throws InterruptedException {
        // given
        String key = "feed:user:1";

        // when // then
        redisUtil.setBit(key, 1, true, 1, TimeUnit.SECONDS);
        MILLISECONDS.sleep(500);
        assertThat(redisUtil.hasKey(key)).isTrue();

        redisUtil.getBit(key, 1, 1, TimeUnit.SECONDS);
        MILLISECONDS.sleep(500);
        assertThat(redisUtil.hasKey(key)).isTrue();
        MILLISECONDS.sleep(500);
        assertThat(redisUtil.hasKey(key)).isFalse();
    }

    @Test
    @DisplayName("bit 값이 1인 id를 가져올 수 있다 - case 1")
    void findIdsOfTrueBits() {
        // given
        String key = "feed:user:1";
        redisUtil.setBit(key, 2, true);
        redisUtil.setBit(key, 3, true);
        redisUtil.setBit(key, 5, true);
        redisUtil.setBit(key, 10, true);
        redisUtil.setBit(key, 11, true);
        redisUtil.setBit(key, 14, true);

        // when
        Set<Long> trueBits = redisUtil.findIdsOfTrueBits(key);

        // then
        assertThat(trueBits.size()).isEqualTo(6);
        assertThat(trueBits).contains(2l, 3l, 5l, 10l, 11l, 14l);
    }

    @Test
    @DisplayName("bit 값이 1인 id를 가져올 수 있다 - case 2")
    void findIdsOfTrueBits2() {
        // given
        String key = "feed:user:1";
        redisUtil.setBit(key, 5, true);
        redisUtil.setBit(key, 2, true);
        redisUtil.setBit(key, 3, true);
        redisUtil.setBit(key, 9, true);
        redisUtil.setBit(key, 14, true);

        // when
        Set<Long> trueBits = redisUtil.findIdsOfTrueBits(key);

        // then
        assertThat(trueBits.size()).isEqualTo(5);
        assertThat(trueBits).contains(2l, 3l, 5l, 9l, 14l);
    }

    @Test
    @DisplayName("리스트 콜렉션의 데이터를 추가한다")
    void setList() {
        // given
        String key = "rank:user";

        // when
        redisUtil.setList(key, List.of(1L, 2L, 3L, 4L, 5L));

        // then
        List<Long> list = redisUtil.getList(key, 0, 3);
        assertThat(list).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("리스트에 value 추가")
    void pushElem() {
        //given
        String key = "feed:1";

        // when
        redisUtil.pushElementWithLimit(key, 1L, 100);
        redisUtil.pushElementWithLimit(key, 2L, 100);

        // then
        List<Long> list = redisUtil.getList(key, 0, 2);
        assertThat(list).containsExactly(2L, 1L);
    }

    @Test
    @DisplayName("웹툰 피드 Expire 타임 테스트")
    void pushElemWithTimeLimit() throws InterruptedException {
        //given
        String key = "feed:2";

        // when
        redisUtil.pushElementWithLimit(key, 1L, 100);
        List<Long> list1 = redisUtil.getList(key, 0, 2, 1, TimeUnit.SECONDS);

        redisUtil.pushElementWithLimit(key, 2L, 100);
        List<Long> list2 = redisUtil.getList(key, 0, 2, 1, TimeUnit.SECONDS);

        // then
        MILLISECONDS.sleep(1000);
        assertThat(list1).containsExactly(1L);
        assertThat(list2).containsExactly(2L, 1L);
        assertThat(redisUtil.hasKey(key)).isFalse();
    }
}