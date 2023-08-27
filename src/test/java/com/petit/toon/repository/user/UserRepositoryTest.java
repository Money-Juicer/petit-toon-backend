package com.petit.toon.repository.user;

import com.petit.toon.DefaultProfileImageSetup;
import com.petit.toon.config.QueryDslConfig;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.List;

import static com.petit.toon.service.user.ProfileImageService.DEFAULT_PROFILE_IMAGE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@ActiveProfiles("test")
@Import({DefaultProfileImageSetup.class, QueryDslConfig.class})
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileImageRepository profileImageRepository;

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

    @Test
    @DisplayName("주어진 id에 해당하는 유저 엔티티를 조회한다")
    void findAllWithProfileImageWithExactOrder() {
        // given
        User user1 = createUserWithProfileImage("sample_user_1", DEFAULT_PROFILE_IMAGE_ID);
        User user2 = createUserWithProfileImage("sample_user_2", DEFAULT_PROFILE_IMAGE_ID);
        User user3 = createUserWithProfileImage("sample_user_3", DEFAULT_PROFILE_IMAGE_ID);
        User user4 = createUserWithProfileImage("sample_user_4", DEFAULT_PROFILE_IMAGE_ID);

        // when
        List<User> users = userRepository.findAllWithProfileImageWithExactOrder(List.of(user2.getId(), user4.getId()));

        // then
        assertThat(users).extracting("id", "nickname")
                .containsExactly(
                        tuple(user2.getId(), user2.getNickname()),
                        tuple(user4.getId(), user4.getNickname())
                );
    }

    private User createUser(String nickname) {
        return userRepository.save(User.builder()
                .nickname(nickname)
                .build());
    }

    private User createUserWithProfileImage(String nickname, Long profileImageId) {
        User user = userRepository.save(User.builder()
                .nickname(nickname)
                .build());
        ProfileImage profileImage = profileImageRepository.findById(profileImageId).get();
        user.setProfileImage(profileImage);
        return userRepository.save(user);
    }
}