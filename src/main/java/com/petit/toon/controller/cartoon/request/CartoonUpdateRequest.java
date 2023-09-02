package com.petit.toon.controller.cartoon.request;

import com.petit.toon.service.cartoon.request.CartoonUpdateServiceRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartoonUpdateRequest {
    private String title;
    private String description;

    @Builder
    private CartoonUpdateRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public CartoonUpdateServiceRequest toServiceRequest(long userId, long toonId) {
        return CartoonUpdateServiceRequest.builder()
                .userId(userId)
                .toonId(toonId)
                .title(title)
                .description(description)
                .build();
    }
}
