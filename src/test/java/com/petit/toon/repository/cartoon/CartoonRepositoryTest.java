package com.petit.toon.repository.cartoon;

import com.petit.toon.config.QueryDslConfig;
import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
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

    private User createUser(String nickname) {
        return userRepository.save(
                User.builder()
                        .nickname(nickname)
                        .build());
    }

    private Cartoon createCartoon(User user, String title) {
        return cartoonRepository.save(
                Cartoon.builder()
                        .user(user)
                        .title(title)
                        .build());
    }
}
