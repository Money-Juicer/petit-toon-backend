package com.petit.toon.service.rank;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.rank.RankRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.response.CartoonResponse;
import com.petit.toon.service.rank.response.CartoonRankResponse;
import com.petit.toon.service.rank.response.UserRankResponse;
import com.petit.toon.service.user.response.UserResponse;
import com.petit.toon.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankService {
    public static final String USER_RANK_KEY = "rank:user";
    public static final String CARTOON_RANK_KEY = "rank:toon";

    private final UserRepository userRepository;
    private final CartoonRepository cartoonRepository;
    private final RankRepository rankRepository;
    private final RedisUtil redisUtil;

    public UserRankResponse userRank(Pageable pageable, LocalDateTime period) {
        if (redisUtil.hasKey(USER_RANK_KEY)) {
            List<Long> userIds = redisUtil.getList(USER_RANK_KEY,
                    pageable.getPageSize() * pageable.getPageNumber(),
                    pageable.getPageSize() * (pageable.getPageNumber() + 1));
            return toUserRankResponse(userIds);
        }
        List<Long> userIds = rankRepository.findUserRank(pageable,
                period.with(LocalTime.MIN).minusDays(7));

        if (userIds.isEmpty()) {
            return new UserRankResponse(List.of());
        }

        redisUtil.setList(USER_RANK_KEY, userIds, 1, TimeUnit.HOURS);
        return toUserRankResponse(userIds);
    }

    public CartoonRankResponse cartoonRank(Pageable pageable, LocalDateTime period) {
        if (redisUtil.hasKey(CARTOON_RANK_KEY)) {
            List<Long> cartoonIds = redisUtil.getList(CARTOON_RANK_KEY,
                    pageable.getPageSize() * pageable.getPageNumber(),
                    pageable.getPageSize() * (pageable.getPageNumber() + 1));
            return toCartoonRankResponse(cartoonIds);
        }
        List<Long> cartoonIds = rankRepository.findCartoonRank(pageable,
                period.with(LocalTime.MIN).minusDays(7));

        if (cartoonIds.isEmpty()) {
            return new CartoonRankResponse(List.of());
        }

        redisUtil.setList(CARTOON_RANK_KEY, cartoonIds, 1, TimeUnit.HOURS);
        return toCartoonRankResponse(cartoonIds);
    }

    private UserRankResponse toUserRankResponse(List<Long> userIds) {
        List<User> users = userRepository.findAllWithProfileImageWithExactOrder(userIds);
        List<UserResponse> userRank = users.stream()
                .map(UserResponse::of)
                .collect(Collectors.toList());
        return new UserRankResponse(userRank);
    }

    private CartoonRankResponse toCartoonRankResponse(List<Long> cartoonIds) {
        List<Cartoon> cartoons = cartoonRepository.findAllWithUserWithExactOrder(cartoonIds);
        List<CartoonResponse> cartoonRank = cartoons.stream()
                .map(CartoonResponse::of)
                .collect(Collectors.toList());
        return new CartoonRankResponse(cartoonRank);
    }
}
