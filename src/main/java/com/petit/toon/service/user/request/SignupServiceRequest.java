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
    private String email;
    private String password;

    @Builder
    private SignupServiceRequest(String name, String nickname, String email, String password) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }
}
