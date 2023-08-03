package com.petit.toon.repository.user;

import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void findUserById() {
        // given
        User user = User.builder()
                .name("김지훈")
                .build();
        String absolutePath = new File("src/test/resources/sample-profile-images/default.png").getAbsolutePath();
        ProfileImage profileImage = ProfileImage.builder()
                .fileName("deafult.png")
                .path(absolutePath)
                .build();
        user.setProfileImage(profileImage);
        userRepository.save(user);

        // when
        User findUser = userRepository.findUserById(user.getId()).get();

        // then
        assertThat(findUser.getProfileImage()).isEqualTo(profileImage);
        assertThat(findUser.getProfileImage().getPath()).isEqualTo(profileImage.getPath());

    }

    @Test
    @DisplayName("주어진 id에 해당하는 유저들을 조회한다")
    void findAllByIdIn() {
        // given
        User user1 = createUser("@hotoran");
        User user2 = createUser("@timel2ss");
        User user3 = createUser("@Iced");

        List<Long> ids = List.of(user2.getId(), user3.getId());

        // when
        List<User> findUsers = userRepository.findAllByIdIn(ids);

        // then
        assertThat(findUsers).contains(user2, user3);
    }

    private User createUser(String nickname) {
        return userRepository.save(User.builder()
                .nickname(nickname)
                .build());
    }
}