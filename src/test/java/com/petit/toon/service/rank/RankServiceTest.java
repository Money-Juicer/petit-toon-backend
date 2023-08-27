package com.petit.toon.service.rank;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Like;
import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.cartoon.LikeRepository;
import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.repository.user.ProfileImageRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.rank.response.CartoonRankResponse;
import com.petit.toon.service.rank.response.UserRankResponse;
import com.petit.toon.util.RedisUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.petit.toon.service.user.ProfileImageService.DEFAULT_PROFILE_IMAGE_ID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class RankServiceTest {

    @Autowired
    RankService rankService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileImageRepository profileImageRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    RedisUtil redisUtil;

    @AfterEach
    void tearDown() {
        redisUtil.flushAll();
    }

    @Test
    @DisplayName("사용자의 주간 랭킹을 조회한다")
    void userRank() {
        // given
        User user1 = createUser("@Hotoran1");
        User user2 = createUser("@Hotoran2");
        User user3 = createUser("@Hotoran3");
        User user4 = createUser("@Hotoran3");

        createFollow(user2, user1, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user1, user2, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user3, user2, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user4, user2, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user1, user3, LocalDateTime.of(2023, 8, 25, 3, 26));
        createFollow(user4, user3, LocalDateTime.of(2023, 8, 25, 3, 26));

        PageRequest pageRequest = PageRequest.of(0, 3);

        // when
        UserRankResponse response = rankService.userRank(pageRequest,
                LocalDateTime.of(2023, 8, 25, 10, 30));

        // then
        assertThat(response.getUsers()).extracting("id")
                .containsExactly(user2.getId(), user3.getId(), user1.getId());
    }

    @Test
    @DisplayName("사용자의 주간 랭킹을 조회한다 using Cache")
    void userRank2() {
        // given
        User user1 = createUser("@Hotoran1");
        User user2 = createUser("@Hotoran2");
        User user3 = createUser("@Hotoran3");
        User user4 = createUser("@Hotoran4");

        PageRequest pageRequest = PageRequest.of(0, 3);

        redisUtil.setList("rank:user", List.of(user2.getId(), user3.getId(), user1.getId(), user4.getId()));

        // when
        UserRankResponse response = rankService.userRank(pageRequest,
                LocalDateTime.of(2023, 8, 25, 10, 30));

        // then
        assertThat(response.getUsers()).extracting("id")
                .containsExactly(user2.getId(), user3.getId(), user1.getId());
    }

    @Test
    @DisplayName("웹툰의 주간 랭킹을 조회한다")
    void cartoonRank() {
        // given
        User user1 = createUser("@Hotoran1");
        User user2 = createUser("@Hotoran2");
        User user3 = createUser("@Hotoran3");
        User user4 = createUser("@Hotoran4");

        Cartoon cartoon1 = createCartoon(user1, "김영현의 모험1");
        Cartoon cartoon2 = createCartoon(user1, "김영현의 모험2");
        Cartoon cartoon3 = createCartoon(user1, "김영현의 모험3");

        createLike(user2, cartoon1, LocalDateTime.of(2023, 8, 25, 3, 26));
        createLike(user3, cartoon1, LocalDateTime.of(2023, 8, 25, 3, 26));
        createLike(user3, cartoon2, LocalDateTime.of(2023, 8, 25, 3, 26));
        createLike(user1, cartoon3, LocalDateTime.of(2023, 8, 25, 3, 26));
        createLike(user3, cartoon3, LocalDateTime.of(2023, 8, 25, 3, 26));
        createLike(user4, cartoon3, LocalDateTime.of(2023, 8, 25, 3, 26));

        PageRequest pageRequest = PageRequest.of(0, 5);

        // when
        CartoonRankResponse response = rankService.cartoonRank(pageRequest,
                LocalDateTime.of(2023, 8, 25, 4, 21));

        // then
        assertThat(response.getCartoons()).extracting("id")
                .containsExactly(cartoon3.getId(), cartoon1.getId(), cartoon2.getId());
    }

    @Test
    @DisplayName("웹툰의 주간 랭킹을 조회한다 with Cache")
    void cartoonRank2() {
        // given
        User user1 = createUser("@Hotoran1");

        Cartoon cartoon1 = createCartoon(user1, "김영현의 모험1");
        Cartoon cartoon2 = createCartoon(user1, "김영현의 모험2");
        Cartoon cartoon3 = createCartoon(user1, "김영현의 모험3");

        redisUtil.setList("rank:toon", List.of(cartoon3.getId(), cartoon1.getId(), cartoon2.getId()));

        PageRequest pageRequest = PageRequest.of(0, 5);

        // when
        CartoonRankResponse response = rankService.cartoonRank(pageRequest,
                LocalDateTime.of(2023, 8, 25, 4, 21));

        // then
        assertThat(response.getCartoons()).extracting("id")
                .containsExactly(cartoon3.getId(), cartoon1.getId(), cartoon2.getId());
    }

    private User createUser(String tag) {
        User user = userRepository.save(User.builder()
                .tag(tag)
                .build());
        ProfileImage profileImage = profileImageRepository.findById(DEFAULT_PROFILE_IMAGE_ID).get();
        user.setProfileImage(profileImage);
        return userRepository.save(user);
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