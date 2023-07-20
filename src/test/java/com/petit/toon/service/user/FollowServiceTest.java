package com.petit.toon.service.user;

import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FollowServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    FollowService followService;

    @AfterEach
    void tearDown() {
        followRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("사용자는 특정 유저를 팔로우할 수 있다")
    void follow() {
        // given
        User user1 = createUser("KIM");
        User user2 = createUser("LEE");

        userRepository.save(user1);
        userRepository.save(user2);

        // when
        long followId = followService.follow(user1.getId(), user2.getId());

        // then
        Follow follow = followRepository.findById(followId).get();
        assertThat(follow.getId()).isEqualTo(1l);
        assertThat(follow.getFollower().getId()).isEqualTo(user1.getId());
        assertThat(follow.getFollowee().getId()).isEqualTo(user2.getId());
    }

    private User createUser(String name) {
        return User.builder()
                .name(name)
                .build();
    }

}