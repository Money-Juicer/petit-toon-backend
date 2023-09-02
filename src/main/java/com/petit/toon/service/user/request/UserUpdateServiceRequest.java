package com.petit.toon.service.user.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserUpdateServiceRequest {
    private long userId;
    private String nickname;
    private String tag;
    private String password;
    private String statusMessage;

    @Builder
    private UserUpdateServiceRequest(long userId, String nickname, String tag, String password, String statusMessage) {
        this.userId = userId;
        this.nickname = nickname;
        this.tag = tag;
        this.password = password;
        this.statusMessage = statusMessage;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
