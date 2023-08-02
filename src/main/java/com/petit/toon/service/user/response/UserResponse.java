package com.petit.toon.service.user.response;

import com.petit.toon.entity.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponse {

    private long id;
    private String nickname;
    private String profileImagePath;

    @Builder
    private UserResponse(long id, String nickname, String profileImagePath) {
        this.id = id;
        this.nickname = nickname;
        this.profileImagePath = profileImagePath;
    }

    public static UserResponse of(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImagePath(user.getProfileImage().getPath())
                .build();
    }
}
