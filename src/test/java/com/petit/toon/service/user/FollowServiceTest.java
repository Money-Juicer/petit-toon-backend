package com.petit.toon.service.user;

import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.repository.user.ProfileImageRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.response.UserListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
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

    @Test
    @DisplayName("사용자는 특정 유저를 팔로우할 수 있다")
    void follow() {
        // given
        User user1 = createUser("KIM");
        User user2 = createUser("LEE");

        // when
        followService.follow(user1.getId(), user2.getId());

        // then
        Follow follow = followRepository.findByFollowerIdAndFolloweeId(user1.getId(), user2.getId()).get();
        assertThat(follow.getFollower().getId()).isEqualTo(user1.getId());
        assertThat(follow.getFollowee().getId()).isEqualTo(user2.getId());
    }

    @Test
    @DisplayName("팔로우는 중복해서 생성될 수 없다")
    void follow2() {
        // given
        User user1 = createUser("KIM");
        User user2 = createUser("LEE");

        // when // then
        followService.follow(user1.getId(), user2.getId());
        assertThatThrownBy(() -> followService.follow(user1.getId(), user2.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("내가 팔로우 하는 유저 정보를 가져온다")
    void findFollowingUsers() {
        // given
        User user1 = createUser("김지훈");
        User user2 = createUser("이용우");
        User user3 = createUser("김승환");

        createFollow(user1, user2);
        createFollow(user1, user3);

        PageRequest pageRequest = PageRequest.of(0, 20);

        // when
        UserListResponse followingUsers = followService.findFollowingUsers(user1.getId(), pageRequest);

        // then
        assertThat(followingUsers.getUsers().size()).isEqualTo(2);
        assertThat(followingUsers.getUsers()).extracting("id", "nickname")
                .contains(
                        tuple(user2.getId(), "이용우"),
                        tuple(user3.getId(), "김승환")
                );
    }

    @Test
    @DisplayName("나를 팔로우 하는 유저 정보를 가져온다")
    void findFollowedUsers() {
        // given
        User user1 = createUser("김지훈");
        User user2 = createUser("이용우");
        User user3 = createUser("김승환");

        createFollow(user2, user1);
        createFollow(user3, user1);

        PageRequest pageRequest = PageRequest.of(0, 20);

        // when
        UserListResponse followingUsers = followService.findFollowedUsers(user1.getId(), pageRequest);

        // then
        assertThat(followingUsers.getUsers().size()).isEqualTo(2);
        assertThat(followingUsers.getUsers()).extracting("id", "nickname")
                .contains(
                        tuple(user2.getId(), "이용우"),
                        tuple(user3.getId(), "김승환")
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
        followService.unfollow(user1.getId(), user2.getId());

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