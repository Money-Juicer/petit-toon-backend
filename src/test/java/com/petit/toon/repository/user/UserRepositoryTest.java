package com.petit.toon.repository.user;

import com.petit.toon.DefaultProfileImageSetup;
import com.petit.toon.config.QueryDslConfig;
import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.petit.toon.service.user.ProfileImageService.DEFAULT_PROFILE_IMAGE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@ActiveProfiles("test")
@Import({DefaultProfileImageSetup.class, QueryDslConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FollowRepository followRepository;

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

    @Test
    @DisplayName("팔로워 수가 10,000명 이상이면 influencer가 된다")
    void updateInfluenceStatus() {
        // given
        User user = createUser("Hotoran");
        createUsers(10_000);
        createFollows(user);

        // when
        long updatedCount = userRepository.updateInfluenceStatus(true);
        em.clear();

        // then
        User findUser = userRepository.findById(user.getId()).get();
        assertThat(updatedCount).isEqualTo(1L);
        assertThat(findUser.isInfluencer()).isTrue();
    }

    @Test
    @DisplayName("팔로워 수가 10,000명 이하면 influencer가 아니다")
    void updateInfluenceStatus2() {
        // given
        User user = createUser("Hotoran");
        user.updateInfluenceStatus(true);
        userRepository.save(user);

        User follower = createUser("follower");
        createFollow(follower, user);

        // when
        long updatedCount = userRepository.updateInfluenceStatus(false);
        em.clear();

        // then
        User findUser = userRepository.findById(user.getId()).get();
        assertThat(updatedCount).isEqualTo(1L);
        assertThat(findUser.isInfluencer()).isFalse();
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

    private Follow createFollow(User follower, User followee) {
        return followRepository.save(
                Follow.builder()
                        .follower(follower)
                        .followee(followee)
                        .build());
    }

    private void createUsers(int number) {
        userRepository.bulkInsert(
                IntStream.range(1, number + 1)
                        .mapToObj(i -> User.builder()
                                .nickname("follower" + i)
                                .build())
                        .collect(Collectors.toList()));
    }

    private void createFollows(User followee) {
        List<User> followers = userRepository.findAll().stream()
                .filter(user -> !user.equals(followee))
                .collect(Collectors.toList());

        followRepository.bulkInsert(followers.stream()
                .map(follower -> Follow.builder()
                        .follower(follower)
                        .followee(followee)
                        .build())
                .collect(Collectors.toList()));
    }
}