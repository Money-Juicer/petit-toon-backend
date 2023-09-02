package com.petit.toon.repository.cartoon;

import com.petit.toon.config.QueryDslConfig;
import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
public class CartoonRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Autowired
    FollowRepository followRepository;

    @Test
    void findCartoonById() {
        //given
        User user = User.builder()
                .nickname("KIM")
                .build();

        String absolutePath = new File("src/test/resources/sample-profile-images/default.png").getAbsolutePath();
        ProfileImage profileImage = ProfileImage.builder()
                .fileName("deafult.png")
                .path(absolutePath)
                .build();
        user.setProfileImage(profileImage);
        userRepository.save(user);

        Cartoon toon = cartoonRepository.save(Cartoon.builder()
                .title("title")
                .description("sample")
                .user(user)
                .build());

        //when
        Cartoon result = cartoonRepository.findCartoonById(toon.getId()).get();

        //then
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getUser().getProfileImage()).isEqualTo(profileImage);

    }

    @Test
    @DisplayName("주어진 id에 해당하는 웹툰 엔티티를 조회한다")
    void findAllWithUserByIdIn() {
        // given
        User user = createUser("KIM");
        Cartoon cartoon1 = createCartoon(user, "김영현의 모험1");
        Cartoon cartoon2 = createCartoon(user, "김영현의 모험2");
        Cartoon cartoon3 = createCartoon(user, "김영현의 모험3");
        Cartoon cartoon4 = createCartoon(user, "김영현의 모험4");

        // when
        List<Cartoon> cartoons = cartoonRepository.findAllWithUserWithExactOrder(List.of(cartoon1.getId(), cartoon3.getId()));

        // then
        assertThat(cartoons).extracting("id", "title")
                .containsExactly(
                        tuple(cartoon1.getId(), cartoon1.getTitle()),
                        tuple(cartoon3.getId(), cartoon3.getTitle()));
    }

    @Test
    @DisplayName("팔로우 한 인플루언서의 작품을 조회한다")
    void findAllWithFollower() {
        // given
        User user = createUser("Hotoran");
        User author1 = createInfluencer("influencer1");
        User author2 = createInfluencer("influencer2");
        User author3 = createInfluencer("influencer3");
        User author4 = createInfluencer("influencer4");
        User author5 = createUser("author5");

        createFollow(user, author1);
        createFollow(user, author2);
        createFollow(user, author3);
        createFollow(user, author4);
        createFollow(user, author5);

        Cartoon cartoon1 = createCartoon(author1, "title1");
        Cartoon cartoon2 = createCartoon(author1, "title2");
        Cartoon cartoon3 = createCartoon(author2, "title3");
        Cartoon cartoon4 = createCartoon(author3, "title4");
        Cartoon cartoon5 = createCartoon(author3, "title5");
        Cartoon cartoon6 = createCartoon(author3, "title6");
        Cartoon cartoon7 = createCartoon(author4, "title7");
        Cartoon cartoon8 = createCartoon(author4, "title8");
        Cartoon cartoon9 = createCartoon(author5, "title9");
        Cartoon cartoon10 = createCartoon(author5, "title10");

        PageRequest request = PageRequest.of(0, 10);
        // when
        List<Cartoon> followCartoons = cartoonRepository.findAllWithFollower(user.getId(), request);

        // then
        assertThat(followCartoons.size()).isEqualTo(8);
        assertThat(followCartoons).doesNotContain(cartoon9, cartoon10);
    }

    private User createUser(String nickname) {
        return userRepository.save(
                User.builder()
                        .nickname(nickname)
                        .build());
    }

    private User createInfluencer(String nickname) {
        User user = User.builder()
                .nickname(nickname)
                .build();
        user.updateInfluenceStatus(true);
        return userRepository.save(user);
    }

    private Cartoon createCartoon(User user, String title) {
        return cartoonRepository.save(
                Cartoon.builder()
                        .user(user)
                        .title(title)
                        .build());
    }

    private Follow createFollow(User follower, User followee) {
        return followRepository.save(
                Follow.builder()
                        .follower(follower)
                        .followee(followee)
                        .build());
    }
}
