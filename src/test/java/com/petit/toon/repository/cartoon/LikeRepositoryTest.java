package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Like;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class LikeRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Test
    @DisplayName("cartoon Id로 좋아요를 누른 유저 id를 조회한다")
    void findUserIdByCartoonId() {
        // given
        User user1 = createUser("@hotoran");
        User user2 = createUser("@timel2ss");
        User user3 = createUser("@Iced");

        Cartoon cartoon = createToon(user1, "sample-title");
        createLike(user2, cartoon);
        createLike(user3, cartoon);

        // when
        List<Long> userIds = likeRepository.findUserIdByCartoonId(cartoon.getId());

        // then
        assertThat(userIds).contains(user2.getId(), user3.getId());
    }

    private Like createLike(User user2, Cartoon cartoon) {
        return likeRepository.save(Like.builder()
                .user(user2)
                .cartoon(cartoon)
                .build());
    }

    private Cartoon createToon(User user, String title) {
        return cartoonRepository.save(Cartoon.builder()
                .user(user)
                .title(title)
                .build());
    }

    private User createUser(String nickname) {
        return userRepository.save(User.builder()
                .nickname(nickname)
                .build());
    }
}