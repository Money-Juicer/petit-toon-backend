package com.petit.toon.service.search;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.user.ProfileImageRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.response.CartoonListResponse;
import com.petit.toon.service.user.ProfileImageService;
import com.petit.toon.service.user.response.UserListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SearchServiceTest {
    @Autowired
    SearchService searchService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileImageRepository profileImageRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Test
    @DisplayName("keyword로 유저 정보를 검색한다")
    void searchUser() {
        // given
        User user1 = createUser("김승환", "김영현 광팬");
        User user2 = createUser("김영현", "Kinggg");

        // when
        UserListResponse searchResponse = searchService.searchUser("김영현", PageRequest.of(0, 10));

        // then
        assertThat(searchResponse.getUsers())
                .extracting("id")
                .containsExactly(user1.getId());
    }

    @Test
    @DisplayName("keyword로 만화 정보를 검색한다")
    void searchCartoon() {
        // given
        User user1 = createUser("김승환", "김영현 광팬");
        User user2 = createUser("김영현", "Kinggg");

        Cartoon toon1 = createToon(user2, "김영현의 모험", "용사 김영현이 모험을 떠난다");
        Cartoon toon2 = createToon(user1, "최애의 아이", "궁극의 아이돌 김영현");
        Cartoon toon3 = createToon(user2, "용사 김영현", "김영현의 모험 2탄");

        // when
        CartoonListResponse searchResponse = searchService.searchCartoon("김영현", PageRequest.of(0, 10));

        // then
        assertThat(searchResponse.getCartoons())
                .extracting("id")
                .containsExactly(toon1.getId(), toon3.getId());
    }

    private Cartoon createToon(User user, String title, String description) {
        Cartoon cartoon = cartoonRepository.save(Cartoon.builder()
                .user(user)
                .title(title)
                .description(description)
                .build());
        cartoonRepository.flush();
        return cartoon;
    }

    private User createUser(String name, String nickname) {
        ProfileImage defaultImage = profileImageRepository.findById(ProfileImageService.DEFAULT_PROFILE_IMAGE_ID).get();
        User user = userRepository.save(User.builder()
                .name(name)
                .nickname(nickname)
                .build());
        user.setProfileImage(defaultImage);
        return userRepository.save(user);
    }
}