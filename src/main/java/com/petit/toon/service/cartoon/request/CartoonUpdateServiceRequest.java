package com.petit.toon.service.cartoon.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartoonUpdateServiceRequest {
    private long userId;
    private long toonId;
    private String title;
    private String description;

    @Builder
    private CartoonUpdateServiceRequest(long userId, long toonId, String title, String description) {
        this.userId = userId;
        this.toonId = toonId;
        this.title = title;
        this.description = description;
    }
}
