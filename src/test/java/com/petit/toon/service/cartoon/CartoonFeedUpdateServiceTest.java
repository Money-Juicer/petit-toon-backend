package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.util.RedisUtil;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.petit.toon.service.feed.FeedService.FEED_KEY_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartoonFeedUpdateServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    CartoonFeedUpdateService cartoonFeedUpdateService;

    @AfterEach
    void tearDown() {
        redisUtil.flushAll();
    }

    @Test
    @DisplayName("팔로워에게 피드 푸쉬")
    void feedUpdateToFollower() {
        // given
        User user1 = createUser("Hotoran"); // Hotoran : Followee1
        User user2 = createUser("A");       //       A : Followee2
        User user3 = createUser("B");
        User user4 = createUser("C");
        User user5 = createUser("D");

        createFollows(List.of(user1, user2, user3, user4, user5));

        Cartoon cartoon1 = createCartoon(user1);
        Cartoon cartoon2 = createCartoon(user1);
        Cartoon cartoon3 = createCartoon(user2);

        // when
        cartoonFeedUpdateService.feedUpdateToFollower(user1.getId(), cartoon1.getId());
        cartoonFeedUpdateService.feedUpdateToFollower(user1.getId(), cartoon2.getId());
        cartoonFeedUpdateService.feedUpdateToFollower(user2.getId(), cartoon3.getId());

        // then
        List<Long> list1 = redisUtil.getList(FEED_KEY_PREFIX + user1.getId(), 0, 0);
        List<Long> list2 = redisUtil.getList(FEED_KEY_PREFIX + user2.getId(), 0, 0);
        List<Long> list3 = redisUtil.getList(FEED_KEY_PREFIX + user3.getId(), 0, 0);
        List<Long> list4 = redisUtil.getList(FEED_KEY_PREFIX + user4.getId(), 0, 0);
        List<Long> list5 = redisUtil.getList(FEED_KEY_PREFIX + user5.getId(), 0, 0);

        assertThat(list1.size()).isEqualTo(1);
        assertThat(list1.get(0)).isEqualTo(cartoon3.getId());

        assertThat(list2.size()).isEqualTo(2);
        assertThat(list2).contains(cartoon1.getId(), cartoon2.getId());

        assertThat(list3.size()).isEqualTo(3);
        assertThat(list4.size()).isEqualTo(3);
        assertThat(list5.size()).isEqualTo(3);

        assertThat(list3).contains(cartoon1.getId(), cartoon2.getId(), cartoon3.getId());
    }

    private User createUser(String nickname) {
        return userRepository.save(User.builder()
                .nickname(nickname)
                .build());
    }

    private void createFollows(List<User> users) {
        User followee1 = users.get(0);
        User followee2 = users.get(1);

        for (User user : users) {
            if (followee1.getId() == user.getId()) {
                continue;
            }
            followRepository.save(Follow.builder()
                    .followee(followee1)
                    .follower(user)
                    .build());
        }

        for (User user : users) {
            if (followee2.getId() == user.getId()) {
                continue;
            }
            followRepository.save(Follow.builder()
                    .followee(followee2)
                    .follower(user)
                    .build());
        }
    }

    private Cartoon createCartoon(User user) {
        return cartoonRepository.save(Cartoon.builder()
                .title("sample-title")
                .user(user)
                .build());
    }
}