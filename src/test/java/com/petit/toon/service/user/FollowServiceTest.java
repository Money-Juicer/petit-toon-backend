package com.petit.toon.service.user;

import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.repository.user.ProfileImageRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.response.FollowResponse;
import com.petit.toon.service.user.response.FollowUserListResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@ActiveProfiles("test")
class FollowServiceTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileImageRepository profileImageRepository;

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
        FollowResponse response = followService.follow(user1.getId(), user2.getId());

        // then
        Follow follow = followRepository.findById(response.getFollowId()).get();
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

        Follow follow1 = createFollow(user1, user2);
        Follow follow2 = createFollow(user1, user3);

        PageRequest pageRequest = PageRequest.of(0, 20);

        // when
        FollowUserListResponse followingUsers = followService.findFollowingUsers(user1.getId(), pageRequest);

        // then
        assertThat(followingUsers.getFollowUsers().size()).isEqualTo(2);
        assertThat(followingUsers.getFollowUsers()).extracting("followId", "user.id", "user.nickname")
                .contains(
                        tuple(follow1.getId(), user2.getId(), "이용우"),
                        tuple(follow2.getId(), user3.getId(), "김승환")
                );
    }

    @Test
    @DisplayName("팔로우를 취소한다")
    void unfollow() {
        // given
        User user1 = createUser("KIM");
        User user2 = createUser("LEE");

        Follow follow = createFollow(user1, user2);

        // when
        followService.unfollow(follow.getId());

        // then
        List<Follow> follows = followRepository.findAll();
        assertThat(follows).isEmpty();
    }


    private Follow createFollow(User user1, User user2) {
        return followRepository.save(
                Follow.builder()
                        .follower(user1)
                        .followee(user2)
                        .build());
    }

    private User createUser(String nickname) {
        ProfileImage defaultImage = profileImageRepository.findById(ProfileImageService.DEFAULT_PROFILE_IMAGE_ID).get();
        User user = userRepository.save(
                User.builder()
                        .nickname(nickname)
                        .build());
        user.setProfileImage(defaultImage);
        return userRepository.save(user);
    }

}