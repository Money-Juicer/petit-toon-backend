package com.petit.toon.repository.user;

import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FollowRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FollowRepository followRepository;

    @Test
    @DisplayName("자신이 팔로우한 유저의 목록을 가져온다")
    void findByFollowerId() {
        // given
        User user1 = createUser("김지훈");
        User user2 = createUser("김승환");
        User user3 = createUser("김영현");

        followRepository.save(createFollow(user1, user2));
        followRepository.save(createFollow(user1, user3));

        // when
        List<User> followUsers = followRepository.findByFollowerId(user1.getId());

        // then
        assertThat(followUsers.size()).isEqualTo(2);
        assertThat(followUsers).extracting("name").contains("김승환", "김영현");
    }

    private Follow createFollow(User follower, User followee) {
        return Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();
    }

    private User createUser(String name) {
        return userRepository.save(
                User.builder()
                        .name(name)
                        .build());
    }


}