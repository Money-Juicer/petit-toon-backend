package com.petit.toon.service.user.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupServiceRequest {
    private String name;
    private String nickname;
    private String tag;
    private String email;
    private String password;

    @Builder
    private SignupServiceRequest(String name, String nickname, String tag, String email, String password) {
        this.name = name;
        this.nickname = nickname;
        this.tag = tag;
        this.email = email;
        this.password = password;
    }
}
