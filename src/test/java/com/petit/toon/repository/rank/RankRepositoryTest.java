package com.petit.toon.repository.rank;

import com.petit.toon.config.QueryDslConfig;
import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Like;
import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.cartoon.LikeRepository;
import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, RankRepository.class})
class RankRepositoryTest {

    @Autowired
    RankRepository rankRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Autowired
    LikeRepository likeRepository;

    @Test
    @DisplayName("7일간 팔로우가 새로 생성된 수를 세어 정렬한다")
    void findUserRank() {
        // given
        User user1 = createUser("@Hotoran1"); // follow past 6개
        User user2 = createUser("@Hotoran2"); // follow current 2개
        User user3 = createUser("@Hotoran3"); // follow current 4개
        User user4 = createUser("@Hotoran4"); // follow current 6개
        User user5 = createUser("@Hotoran5"); // follow current 3개
        User user6 = createUser("@Hotoran6");
        User user7 = createUser("@Hotoran7");

        // past - 7일 이전의 기록 = 랭킹에 영향 없음
        createFollow(user2, user1, LocalDateTime.of(2023, 8, 10, 3, 26));
        createFollow(user3, user1, LocalDateTime.of(2023, 8, 10, 3, 26));
        createFollow(user4, user1, LocalDateTime.of(2023, 8, 10, 3, 26));
        createFollow(user5, user1, LocalDateTime.of(2023, 8, 10, 3, 26));
        createFollow(user6, user1, LocalDateTime.of(2023, 8, 10, 3, 26));
        createFollow(user7, user1, LocalDateTime.of(2023, 8, 10, 3, 26));

        // current - 랭킹에 반영되는 기록
        createFollow(user1, user2, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user3, user2, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user1, user3, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user2, user3, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user4, user3, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user5, user3, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user1, user4, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user2, user4, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user3, user4, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user5, user4, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user6, user4, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user7, user4, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user1, user5, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user2, user5, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user3, user5, LocalDateTime.of(2023, 8, 25, 3, 26));

        PageRequest pageRequest = PageRequest.of(0, 1000);

        // when
        List<Long> userRank1 = rankRepository.findUserRank(pageRequest,
                LocalDateTime.of(2023, 8, 25, 4, 10)
                        .with(LocalTime.MIN).minusDays(7));

        // then
        assertThat(userRank1)
                .containsExactly(user4.getId(), user3.getId(), user5.getId(), user2.getId());
    }

    @Test
    @DisplayName("팔로우 수가 같으면 최근에 등록된 사용자가 우선순위를 가진다")
    void findUserRank2() {
        // given
        User user1 = createUser("@Hotoran1");
        User user2 = createUser("@Hotoran2");

        createFollow(user2, user1, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user1, user2, LocalDateTime.of(2023, 8, 25, 3, 26));

        PageRequest pageRequest = PageRequest.of(0, 1000);

        // when
        List<Long> userRank = rankRepository.findUserRank(pageRequest,
                LocalDateTime.of(2023, 8, 25, 5, 30)
                        .with(LocalTime.MIN).minusDays(7));

        // then
        assertThat(userRank)
                .containsExactly(user2.getId(), user1.getId());
    }


    @Test
    @DisplayName("7일간 좋아요가 새로 생성된 수를 세어 정렬한다")
    void findCartoonRank() {
        // given
        User user1 = createUser("@Hotoran1");
        User user2 = createUser("@Hotoran2");
        User user3 = createUser("@Hotoran3");
        User user4 = createUser("@Hotoran4");
        User user5 = createUser("@Hotoran5");
        User user6 = createUser("@Hotoran6");
        User user7 = createUser("@Hotoran7");

        Cartoon cartoon1 = createCartoon(user1, "김영현의 모험1"); // like past 6개
        Cartoon cartoon2 = createCartoon(user1, "김영현의 모험2"); // like current 2개
        Cartoon cartoon3 = createCartoon(user1, "김영현의 모험3"); // like current 4개
        Cartoon cartoon4 = createCartoon(user1, "김영현의 모험4"); // like current 6개
        Cartoon cartoon5 = createCartoon(user1, "김영현의 모험5"); // like current 3개

        // past - 7일 이전의 기록 = 랭킹에 영향 없음
        createLike(user2, cartoon1, LocalDateTime.of(2023, 8, 17, 11, 59));
        createLike(user3, cartoon1, LocalDateTime.of(2023, 8, 17, 11, 59));
        createLike(user4, cartoon1, LocalDateTime.of(2023, 8, 17, 11, 59));
        createLike(user5, cartoon1, LocalDateTime.of(2023, 8, 17, 11, 59));
        createLike(user6, cartoon1, LocalDateTime.of(2023, 8, 17, 11, 59));
        createLike(user7, cartoon1, LocalDateTime.of(2023, 8, 17, 11, 59));

        // current - 랭킹에 반영되는 기록
        createLike(user2, cartoon2, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user2, cartoon3, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user2, cartoon4, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user2, cartoon5, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user3, cartoon2, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user3, cartoon3, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user3, cartoon4, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user3, cartoon5, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user4, cartoon3, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user4, cartoon4, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user4, cartoon5, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user5, cartoon3, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user5, cartoon4, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user6, cartoon4, LocalDateTime.of(2023, 8, 18, 0, 0));
        createLike(user7, cartoon4, LocalDateTime.of(2023, 8, 18, 0, 0));

        PageRequest pageRequest = PageRequest.of(0, 1000);

        // when
        List<Long> cartoonRank1 = rankRepository.findCartoonRank(pageRequest,
                LocalDateTime.of(2023, 8, 25, 4, 10)
                        .with(LocalTime.MIN).minusDays(7));

        // then
        assertThat(cartoonRank1)
                .containsExactly(cartoon4.getId(), cartoon3.getId(), cartoon5.getId(), cartoon2.getId());
    }

    private User createUser(String tag) {
        return userRepository.save(
                User.builder()
                        .tag(tag)
                        .build());
    }

    private Follow createFollow(User follower, User followee, LocalDateTime createdDateTime) {
        Follow follow = followRepository.save(
                Follow.builder()
                        .follower(follower)
                        .followee(followee)
                        .build());
        follow.setCreatedDateTime(createdDateTime);
        return followRepository.save(follow);
    }

    private Cartoon createCartoon(User user, String title) {
        return cartoonRepository.save(
                Cartoon.builder()
                        .user(user)
                        .title(title)
                        .build());
    }

    private Like createLike(User user, Cartoon cartoon, LocalDateTime createdDateTime) {
        Like like = likeRepository.save(
                Like.builder()
                        .user(user)
                        .cartoon(cartoon)
                        .build());
        like.setCreatedDateTime(createdDateTime);
        return likeRepository.save(like);
    }
}