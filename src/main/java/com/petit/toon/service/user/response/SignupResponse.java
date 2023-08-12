package com.petit.toon.service.user.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupResponse {
    private long userId;

    public SignupResponse(long userId) {
        this.userId = userId;
    }
}
