package com.petit.toon.service.user.response;

import lombok.Getter;

import java.util.List;

@Getter
public class FollowUserListResponse {
    private List<FollowUserResponse> followUsers;

    public FollowUserListResponse(List<FollowUserResponse> followUsers) {
        this.followUsers = followUsers;
    }
}
