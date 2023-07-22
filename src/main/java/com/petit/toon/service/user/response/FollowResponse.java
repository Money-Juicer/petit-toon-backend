package com.petit.toon.service.user.response;

import lombok.Getter;

@Getter
public class FollowResponse {
    private long followId;

    public FollowResponse(long followId) {
        this.followId = followId;
    }
}
