package com.petit.toon.repository.search;

import com.petit.toon.config.QueryDslConfig;
import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.ToonRepository;
import com.petit.toon.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, SearchRepository.class})
class SearchRepositoryTest {

    @Autowired
    SearchRepository searchRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ToonRepository toonRepository;

    @Test
    @DisplayName("nickname에 keyword가 포함된 유저를 찾는다")
    void searchUser() {
        // given
        User user1 = createUser("김영현", "King");
        User user2 = createUser("영현김", "yhKim");
        User user3 = createUser("김", "Kim");
        User user4 = createUser("현김영", "hky");
        User user5 = createUser("kim", "김");
        User user6 = createUser("kk", "김영현");

        // when
        List<User> findUsers = searchRepository.searchUser(List.of("김"), PageRequest.of(0, 10));

        // then
        assertThat(findUsers.size()).isEqualTo(2);
        assertThat(findUsers).extracting("id")
                .containsExactly(
                        user5.getId(), user6.getId()
                );
    }

    @Test
    @DisplayName("title에 keyword가 포함된 만화를 찾는다")
    void searchCartoon() {
        // given
        User user = createUser("김영현", "김영현");
        Cartoon toon1 = createToon(user, "김영현의 모험", "용사 김영현이 모험을 떠난다");
        Cartoon toon2 = createToon(user, "김영현", "김영현이 모험을 떠난다");
        Cartoon toon3 = createToon(user, "용사 김영현", "용사가 모험을 떠난다");
        Cartoon toon4 = createToon(user, "용사의 모험", "용사 김영현이 모험을 떠난다");
        Cartoon toon5 = createToon(user, "용사의 모험2", "모험을 떠난 용사 김영현");
        Cartoon toon6 = createToon(user, "용사의 모험3", "김영현이 모험을 떠난다");

        // when
        List<Cartoon> findToons = searchRepository.searchCartoon(List.of("김영현"), PageRequest.of(0, 10));

        // then
        assertThat(findToons.size()).isEqualTo(3);
        assertThat(findToons).extracting("id")
                .containsExactly(
                        toon2.getId(), toon1.getId(), toon3.getId()
                );
    }

    @Test
    @DisplayName("title에 keyword가 복수로 포함된 만화를 찾는다")
    void searchCartoon2() {
        // given
        User user = createUser("김영현", "김영현");
        Cartoon toon1 = createToon(user, "용사 김영현의 모험2", "용사 김영현이 모험을 떠난다");
        Cartoon toon2 = createToon(user, "김영현", "김영현이 모험을 떠난다");
        Cartoon toon3 = createToon(user, "용사 김영현", "용사가 모험을 떠난다");
        Cartoon toon4 = createToon(user, "김영현의 모험", "용사 김영현이 모험을 떠난다");
        Cartoon toon5 = createToon(user, "용사의 모험2", "모험을 떠난 용사 김영현");
        Cartoon toon6 = createToon(user, "용사의 모험3", "김영현이 모험을 떠난다");

        // when
        List<Cartoon> findToons = searchRepository.searchCartoon(List.of("김영현", "모험"), PageRequest.of(0, 10));

        // then
        assertThat(findToons.size()).isEqualTo(2);
        assertThat(findToons).extracting("id")
                .containsExactly(
                        toon4.getId(), toon1.getId()
                );
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