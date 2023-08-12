package com.petit.toon.controller.user.request;

import com.petit.toon.service.user.request.ReissueServiceRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReissueRequest {
    @NotBlank
    private String refreshToken;

    @Builder
    private ReissueRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public ReissueServiceRequest toServiceRequest(String clientIp) {
        return ReissueServiceRequest.builder()
                .refreshToken(refreshToken)
                .clientIp(clientIp)
                .build();
    }
}
