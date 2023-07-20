package com.petit.toon.service.user.response;

import com.petit.toon.entity.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponse {

    private long id;
    private String name;
    private String nickname;

    @Builder
    private UserResponse(long id, String name, String nickname) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
    }

    public static UserResponse of(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .build();
    }
}
