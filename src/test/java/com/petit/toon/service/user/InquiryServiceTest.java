package com.petit.toon.service.user;

import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.ProfileImageRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.response.TagExistResponse;
import com.petit.toon.service.user.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class InquiryServiceTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileImageRepository profileImageRepository;

    @Autowired
    InquiryService inquiryService;

    @Test
    @DisplayName("유저 조회 서비스")
    void inquiryByUserId() {
        // given
        User user1 = createUser("지훈", "@Hotoran");
        User user2 = createUser("용우", "@timel2ss");

        // when
        UserResponse response1 = inquiryService.inquiryByUserId(user1.getId());
        UserResponse response2 = inquiryService.inquiryByUserId(user2.getId());

        // then
        assertThat(response1.getNickname()).isEqualTo("지훈");
        assertThat(response1.getTag()).isEqualTo("@Hotoran");
        assertThat(response2.getNickname()).isEqualTo("용우");
        assertThat(response2.getTag()).isEqualTo("@timel2ss");
    }

    @Test
    @DisplayName("유저 태그 중복 체크")
    void checkTagExist() {
        // given
        User user1 = createUser("승환", "@palter00");
        User user2 = createUser("민서", "@Iced0368");
        User user3 = createUser("영현", "@kimye0808");

        // when
        TagExistResponse response1 = inquiryService.checkTagExist("@palter00");
        TagExistResponse response2 = inquiryService.checkTagExist("@Iced0368");
        TagExistResponse response3 = inquiryService.checkTagExist("@kimye");

        // then
        assertThat(response1.isTagExist()).isEqualTo(true);
        assertThat(response2.isTagExist()).isEqualTo(true);
        assertThat(response3.isTagExist()).isEqualTo(false);
    }


    private User createUser(String nickname, String tag) {
        ProfileImage defaultImage = profileImageRepository.findById(ProfileImageService.DEFAULT_PROFILE_IMAGE_ID).get();
        User user = userRepository.save(
                User.builder()
                        .nickname(nickname)
                        .tag(tag)
                        .statusMessage("sample-message")
                        .build());
        user.setProfileImage(defaultImage);
        return userRepository.save(user);
    }
}