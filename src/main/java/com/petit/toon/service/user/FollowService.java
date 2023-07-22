package com.petit.toon.service.user;

import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.response.FollowResponse;
import com.petit.toon.service.user.response.FollowUserListResponse;
import com.petit.toon.service.user.response.FollowUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public FollowResponse follow(long followerId, long followeeId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("User not found. id: " + followerId));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new RuntimeException("User not found. id: " + followeeId));

        Follow follow = Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();

        followRepository.save(follow);
        return new FollowResponse(follow.getId());
    }

    public FollowUserListResponse findFollowingUsers(long userId, Pageable pageable) {
        List<Follow> followingUsers = followRepository.findByFollowerId(userId, pageable);
        List<FollowUserResponse> followUserResponses = followingUsers.stream()
                .map(FollowUserResponse::of)
                .collect(Collectors.toList());
        return new FollowUserListResponse(followUserResponses);
    }

    @Transactional
    public void unfollow(long followId) {
        followRepository.deleteById(followId);
    }
}
