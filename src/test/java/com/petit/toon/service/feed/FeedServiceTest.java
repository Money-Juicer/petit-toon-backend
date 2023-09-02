package com.petit.toon.service.feed;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Dislike;
import com.petit.toon.entity.cartoon.Like;
import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.cartoon.DislikeRepository;
import com.petit.toon.repository.cartoon.LikeRepository;
import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.LikeService;
import com.petit.toon.service.feed.response.FeedResponse;
import com.petit.toon.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FeedServiceTest {
    @Autowired
    CartoonRepository cartoonRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    DislikeRepository dislikeRepository;

    @Autowired
    FeedService feedService;

    @Autowired
    LikeService likeService;

    @Autowired
    RedisUtil redisUtil;

    @Test
    @DisplayName("피드 서비스")
    void feed() {
        // given
        User a = createUser("A");
        User b = createUser("B");
        User c = createUser("C");
        User influencer = createInfluencer("D");

        createFollow(a, b);
        createFollow(a, c);
        createFollow(a, influencer);

        // 2 5 8 / 좋아요 3개 = 0.47
        // 1 4 7 / 시간으로 11시간
        Cartoon cartoon1 = createCartoon(a, b, "title1", LocalDateTime.of(2023, 8, 18, 0, 54, 0));
        Cartoon cartoon2 = createCartoon(a, b, "title2", LocalDateTime.of(2023, 8, 20, 0, 54, 0));
        Cartoon cartoon3 = createCartoon(a, b, "title3", LocalDateTime.of(2023, 9, 2, 0, 54, 0));
        Cartoon cartoon4 = createCartoon(a, c, "title4", LocalDateTime.of(2023, 8, 17, 12, 54, 0));
        Cartoon cartoon5 = createCartoon(a, c, "title5", LocalDateTime.of(2023, 8, 19, 6, 54, 0));
        Cartoon cartoon6 = createCartoon(a, c, "title6", LocalDateTime.of(2023, 9, 1, 12, 54, 0));
        Cartoon cartoon7 = createInfluencerCartoon(influencer, "title7", LocalDateTime.of(2023, 8, 17, 0, 54, 0));
        Cartoon cartoon8 = createInfluencerCartoon(influencer, "title8", LocalDateTime.of(2023, 8, 18, 12, 54, 0));
        Cartoon cartoon9 = createInfluencerCartoon(influencer, "title9", LocalDateTime.of(2023, 9, 1, 0, 54, 0));

        createLike(a, cartoon2);
        createLike(b, cartoon2);
        createLike(c, cartoon2);
        createLike(a, cartoon5);
        createLike(b, cartoon5);
        createLike(c, cartoon5);
        createLike(a, cartoon8);
        createLike(b, cartoon8);
        createLike(c, cartoon8);

        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        FeedResponse response = feedService.feed(pageRequest, a.getId(),
                LocalDateTime.of(2023, 9, 2, 11, 54, 00));

        // then
        assertThat(response.getFeed())
                .containsExactly(cartoon3.getId(), cartoon6.getId(), cartoon9.getId(),
                        cartoon2.getId(), cartoon5.getId(), cartoon8.getId(),
                        cartoon1.getId(), cartoon4.getId(), cartoon7.getId());

    }

    @Test
    @DisplayName("정렬 기준의 점수를 테스트")
    void calculateScore() {
        // given
        User a = createUser("A");

        Cartoon cartoon1 = createInfluencerCartoon(a, "title1", LocalDateTime.of(2022, 9, 2, 11, 54, 00));
        Cartoon cartoon2 = createInfluencerCartoon(a, "title2", LocalDateTime.of(2022, 9, 2, 5, 54, 00));

        // when
        double score1 = feedService.calculateScore(cartoon1);
        double score2 = feedService.calculateScore(cartoon2);

        // then
        assertThat(score1).isGreaterThan(score2);
    }

    private User createUser(String tag) {
        return userRepository.save(userRepository.save(User.builder()
                .tag(tag)
                .build()));
    }

    private User createInfluencer(String nickname) {
        User user = User.builder()
                .nickname(nickname)
                .build();
        user.updateInfluenceStatus(true);
        return userRepository.save(user);
    }

    private Follow createFollow(User follower, User followee) {
        return followRepository.save(
                Follow.builder()
                        .follower(follower)
                        .followee(followee)
                        .build());
    }

    private Cartoon createCartoon(User follower, User author, String title, LocalDateTime createdDateTime) {
        Cartoon cartoon = cartoonRepository.save(
                Cartoon.builder()
                        .user(author)
                        .title(title)
                        .build());
        cartoon.setCreatedDateTime(createdDateTime);
        redisUtil.pushElementWithLimit("feed:" + follower.getId(), cartoon.getId(), 100);
        return cartoonRepository.save(cartoon);
    }

    private Cartoon createInfluencerCartoon(User user, String title, LocalDateTime createdDateTime) {
        Cartoon cartoon = cartoonRepository.save(
                Cartoon.builder()
                        .user(user)
                        .title(title)
                        .build());
        cartoon.setCreatedDateTime(createdDateTime);
        return cartoonRepository.save(cartoon);
    }

    private void createLike(User user, Cartoon cartoon) {
        likeRepository.save(
                Like.builder()
                        .user(user)
                        .cartoon(cartoon)
                        .build());
    }

    private void createDislike(User user, Cartoon cartoon) {
        likeService.dislike(user.getId(), cartoon.getId());
    }


    private void createLikes(Cartoon cartoon, int number) {
        createUsers(number);
        List<User> followers = userRepository.findAll();
        likeRepository.bulkInsert(
                followers.stream()
                        .map(user -> Like.builder()
                                .user(user)
                                .cartoon(cartoon)
                                .build())
                        .collect(Collectors.toList()));
    }

    private void createDislikes(Cartoon cartoon, int number) {
        createUsers(number);
        List<User> followers = userRepository.findAll();
        dislikeRepository.bulkInsert(
                followers.stream()
                        .map(user -> Dislike.builder()
                                .user(user)
                                .cartoon(cartoon)
                                .build())
                        .collect(Collectors.toList()));
    }

    private void createUsers(int number) {
        userRepository.bulkInsert(
                IntStream.range(1, number + 1)
                        .mapToObj(i -> User.builder()
                                .nickname("follower" + i)
                                .build())
                        .collect(Collectors.toList()));
    }
}