package com.petit.toon.service.user.response;

import lombok.Getter;

import java.util.List;

@Getter
public class UserListResponse {
    private List<UserResponse> users;

    public UserListResponse(List<UserResponse> users) {
        this.users = users;
    }
}
