package com.petit.toon.service.user;

import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.FollowRepository;
import com.petit.toon.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public long follow(long followerId, long followeeId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("User not found. id: " + followerId));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new RuntimeException("User not found. id: " + followeeId));

        Follow follow = Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();

        followRepository.save(follow);
        return follow.getId();
    }
}
