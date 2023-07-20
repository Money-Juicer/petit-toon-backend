package com.petit.toon.service.user;

import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.response.UserListResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

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

        // when
        long followId = followService.follow(user1.getId(), user2.getId());

        // then
        Follow follow = followRepository.findById(followId).get();
        assertThat(follow.getFollower().getId()).isEqualTo(user1.getId());
        assertThat(follow.getFollowee().getId()).isEqualTo(user2.getId());
    }

    @Test
    @DisplayName("자신이 팔로우하는 유저 정보를 가져온다")
    void findFollowingUsers() {
        // given
        User user1 = createUser("김지훈");
        User user2 = createUser("이용우");
        User user3 = createUser("김승환");

        createFollow(user1, user2);
        createFollow(user1, user3);

        // when
        UserListResponse followingUsers = followService.findFollowingUsers(user1.getId());

        // then
        assertThat(followingUsers.getUsers().size()).isEqualTo(2);
        assertThat(followingUsers.getUsers()).extracting("id", "name")
                .contains(
                        tuple(2l, "이용우"),
                        tuple(3l, "김승환")
                );
    }

    private Follow createFollow(User user1, User user2) {
        return followRepository.save(
                Follow.builder()
                        .follower(user1)
                        .followee(user2)
                        .build());
    }

    private User createUser(String name) {
        return userRepository.save(
                User.builder()
                        .name(name)
                        .build());
    }

}