package com.petit.toon.service.user.response;

import com.petit.toon.entity.user.Follow;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FollowUserResponse {
    private long followId;
    private UserResponse user;

    @Builder
    private FollowUserResponse(long followId, UserResponse user) {
        this.followId = followId;
        this.user = user;
    }

    public static FollowUserResponse of(Follow follow) {
        return FollowUserResponse.builder()
                .followId(follow.getId())
                .user(UserResponse.of(follow.getFollowee()))
                .build();
    }
}
