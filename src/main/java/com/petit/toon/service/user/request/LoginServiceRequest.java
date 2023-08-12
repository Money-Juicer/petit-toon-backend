package com.petit.toon.service.user.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginServiceRequest {
    private String email;
    private String password;
    private String clientIp;

    @Builder
    private LoginServiceRequest(String email, String password, String clientIp) {
        this.email = email;
        this.password = password;
        this.clientIp = clientIp;
    }
}
