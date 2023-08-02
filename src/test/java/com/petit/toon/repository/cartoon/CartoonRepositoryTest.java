package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
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
}
