package com.petit.toon.service.user.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReissueServiceRequest {
    private String refreshToken;
    private String clientIp;

    @Builder
    private ReissueServiceRequest(String refreshToken, String clientIp) {
        this.refreshToken = refreshToken;
        this.clientIp = clientIp;
    }
}
