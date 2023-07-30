package com.petit.toon.service.search;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.ToonRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.search.response.SearchResponse;
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
    ToonRepository toonRepository;

    @Test
    @DisplayName("keyword로 유저 정보와 만화 정보를 검색한다")
    void search() {
        // given
        User user1 = createUser("김승환", "김영현 광팬");
        User user2 = createUser("김영현", "Kinggg");

        Cartoon toon1 = createToon(user2, "김영현의 모험", "용사 김영현이 모험을 떠난다");
        Cartoon toon2 = createToon(user1, "최애의 아이", "궁극의 아이돌 김영현");
        Cartoon toon3 = createToon(user2, "용사 김영현", "김영현의 모험 2탄");

        // when
        SearchResponse searchResponse = searchService.search("김영현", PageRequest.of(0, 10));

        // then
        assertThat(searchResponse.getUsers())
                .extracting("id")
                .containsExactly(user1.getId());

        assertThat(searchResponse.getToons())
                .extracting("id")
                .containsExactly(toon1.getId(), toon3.getId());
    }

    private Cartoon createToon(User user, String title, String description) {
        return toonRepository.save(Cartoon.builder()
                .user(user)
                .title(title)
                .description(description)
                .build());
    }

    private User createUser(String name, String nickname) {
        return userRepository.save(User.builder()
                .name(name)
                .nickname(nickname)
                .build());
    }
}