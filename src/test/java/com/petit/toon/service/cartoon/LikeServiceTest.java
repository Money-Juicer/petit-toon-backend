package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Like;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.cartoon.LikeRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.repository.util.RedisUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class LikeServiceTest {

    @Autowired
    LikeService likeService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    RedisUtil redisUtil;

    @AfterEach
    void tearDown() {
        redisUtil.flushAll();
    }

    @Test
    @DisplayName("유저 ID와 웹툰 ID로 좋아요를 생성한다")
    void like() {
        // given
        String key = LikeService.LIKE_KEY_PREFIX + 1;

        // when
        likeService.like(1, 1);
        likeService.like(2, 1);
        likeService.like(3, 1);

        // then
        assertThat(redisUtil.countBits(key)).isEqualTo(3);
        assertThat(redisUtil.getBit(key, 1)).isTrue();
        assertThat(redisUtil.getBit(key, 2)).isTrue();
        assertThat(redisUtil.getBit(key, 3)).isTrue();
        assertThat(redisUtil.getBit(key, 4)).isFalse();
    }

    @Test
    @DisplayName("이미 좋아요를 누른 웹툰에 다시 좋아요를 누르면 좋아요가 취소된다")
    void likeTwice() {
        // given
        String key = LikeService.LIKE_KEY_PREFIX + 1;

        // when
        likeService.like(1, 1);
        likeService.like(1, 1);

        // then
        assertThat(redisUtil.countBits(key)).isEqualTo(0);
        assertThat(redisUtil.getBit(key, 1)).isFalse();
    }

    @Test
    @DisplayName("유저 ID와 웹툰 ID로 싫어요를 생성한다")
    void dislike() {
        // given
        String key = LikeService.DISLIKE_KEY_PREFIX + 1;

        // when
        likeService.dislike(2, 1);
        likeService.dislike(3, 1);
        likeService.dislike(1, 1);

        // then
        assertThat(redisUtil.countBits(key)).isEqualTo(3);
        assertThat(redisUtil.getBit(key, 1)).isTrue();
        assertThat(redisUtil.getBit(key, 2)).isTrue();
        assertThat(redisUtil.getBit(key, 3)).isTrue();
        assertThat(redisUtil.getBit(key, 4)).isFalse();
    }

    @Test
    @DisplayName("이미 싫어요를 누른 웹툰에 다시 싫어요를 누르면 싫어요가 취소된다")
    void dislikeTwice() {
        // given
        String key = LikeService.DISLIKE_KEY_PREFIX + 1;

        // when
        likeService.dislike(1, 1);
        likeService.dislike(1, 1);

        // then
        assertThat(redisUtil.countBits(key)).isEqualTo(0);
        assertThat(redisUtil.getBit(key, 1)).isFalse();
    }

    @Test
    @DisplayName("좋아요를 누른 웹툰에 싫어요를 누르면 좋아요가 취소되고 싫어요가 생성된다")
    void likeToggle() {
        // given
        String likeKey = LikeService.LIKE_KEY_PREFIX + 1;
        String dislikeKey = LikeService.DISLIKE_KEY_PREFIX + 1;

        // when
        likeService.like(1, 1);
        likeService.dislike(1, 1);

        // then
        assertThat(redisUtil.countBits(likeKey)).isEqualTo(0);
        assertThat(redisUtil.countBits(dislikeKey)).isEqualTo(1);
        assertThat(redisUtil.getBit(likeKey, 1)).isFalse();
        assertThat(redisUtil.getBit(dislikeKey, 1)).isTrue();
    }

    @Test
    @DisplayName("싫어요를 누른 웹툰에 좋아요를 누르면 싫어요가 취소되고 좋아요가 생성된다")
    void dislikeToggle() {
        // given
        String likeKey = LikeService.LIKE_KEY_PREFIX + 1;
        String dislikeKey = LikeService.DISLIKE_KEY_PREFIX + 1;

        // when
        likeService.dislike(1, 1);
        likeService.like(1, 1);

        // then
        assertThat(redisUtil.countBits(likeKey)).isEqualTo(1);
        assertThat(redisUtil.countBits(dislikeKey)).isEqualTo(0);
        assertThat(redisUtil.getBit(likeKey, 1)).isTrue();
        assertThat(redisUtil.getBit(dislikeKey, 1)).isFalse();
    }

    @Test
    @DisplayName("cache에 데이터가 없다면 db에서 cache로 load한다")
    void loadDataFromDB() {
        // given
        User user1 = createUser("@hotoran");
        User user2 = createUser("@timel2ss");
        User user3 = createUser("@초코송이00");
        User user4 = createUser("@Iced");

        Cartoon cartoon = createToon(user1, "sample-title");
        String key = LikeService.LIKE_KEY_PREFIX + cartoon.getId();

        createLike(user2, cartoon);
        createLike(user3, cartoon);

        // when
        likeService.like(user4.getId(), cartoon.getId());

        // then
        assertThat(redisUtil.countBits(key)).isEqualTo(3);
        assertThat(redisUtil.getBit(key, user1.getId())).isFalse();
        assertThat(redisUtil.getBit(key, user2.getId())).isTrue();
        assertThat(redisUtil.getBit(key, user3.getId())).isTrue();
        assertThat(redisUtil.getBit(key, user4.getId())).isTrue();
    }

    private User createUser(String nickname) {
        return userRepository.save(User.builder()
                .nickname(nickname)
                .build());
    }

    private Cartoon createToon(User user, String title) {
        return cartoonRepository.save(Cartoon.builder()
                .user(user)
                .title(title)
                .build());
    }

    private Like createLike(User user2, Cartoon cartoon) {
        return likeRepository.save(Like.builder()
                .user(user2)
                .cartoon(cartoon)
                .build());
    }
}