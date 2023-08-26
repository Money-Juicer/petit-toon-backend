package com.petit.toon.service.user;

import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.notfound.UserNotFoundException;
import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.response.UserListResponse;
import com.petit.toon.service.user.response.UserResponse;
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
    public void follow(long followerId, long followeeId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(UserNotFoundException::new);
        User followee = userRepository.findById(followeeId)
                .orElseThrow(UserNotFoundException::new);

        Follow follow = Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();

        followRepository.save(follow);
    }

    /**
     * 내가 팔로우 하는 유저 목록 조회
     */
    public UserListResponse findFollowingUsers(long userId, Pageable pageable) {
        List<Follow> follows = followRepository.findByFollowerId(userId, pageable);
        List<UserResponse> response = follows.stream()
                .map(follow -> UserResponse.of(follow.getFollowee()))
                .collect(Collectors.toList());
        return new UserListResponse(response);
    }

    /**
     * 나를 팔로우 하는 유저 목록 조회
     */
    public UserListResponse findFollowedUsers(long userId, Pageable pageable) {
        List<Follow> follows = followRepository.findByFolloweeId(userId, pageable);
        List<UserResponse> response = follows.stream()
                .map(follow -> UserResponse.of(follow.getFollower()))
                .collect(Collectors.toList());
        return new UserListResponse(response);
    }

    @Transactional
    public void unfollow(long followerId, long followeeId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(UserNotFoundException::new);
        User followee = userRepository.findById(followeeId)
                .orElseThrow(UserNotFoundException::new);

        followRepository.deleteByFollowerIdAndFolloweeId(follower.getId(), followee.getId());
    }
}
