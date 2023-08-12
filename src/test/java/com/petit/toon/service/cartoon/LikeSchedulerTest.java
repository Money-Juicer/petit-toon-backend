package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Like;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.cartoon.DislikeRepository;
import com.petit.toon.repository.cartoon.LikeRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.util.RedisUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class LikeSchedulerTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    DislikeRepository dislikeRepository;

    @Autowired
    LikeService likeService;

    @Autowired
    LikeScheduler likeScheduler;

    @Autowired
    RedisUtil redisUtil;

    @AfterEach
    void tearDown() {
        redisUtil.flushAll();
    }

    @Test
    @DisplayName("")
    void syncLike() {
        // given
        User user1 = createUser("@hotoran");
        User user2 = createUser("@timel2ss"); // cache: X, DB: O
        User user3 = createUser("@초코송이00"); // cache: O, DB: O
        User user4 = createUser("@Iced"); // cache: O, DB: X

        Cartoon cartoon = createToon(user1, "sample-title");

        likeService.like(user4.getId(), cartoon.getId());
        likeService.like(user3.getId(), cartoon.getId());
        createLike(user3, cartoon);
        createLike(user2, cartoon);

        // when
        likeScheduler.syncLikeAndDislike();

        // then
        List<Long> userIds = likeRepository.findUserIdByCartoonId(cartoon.getId());
        assertThat(userIds).contains(user3.getId(), user4.getId());
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