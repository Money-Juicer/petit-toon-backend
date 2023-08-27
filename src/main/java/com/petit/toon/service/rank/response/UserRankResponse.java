package com.petit.toon.service.rank.response;

import com.petit.toon.service.user.response.UserResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRankResponse {
    private List<UserResponse> users;

    public UserRankResponse(List<UserResponse> users) {
        this.users = users;
    }
}
