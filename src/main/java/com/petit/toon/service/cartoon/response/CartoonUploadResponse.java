package com.petit.toon.service.cartoon.response;

import com.petit.toon.entity.cartoon.Cartoon;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartoonUploadResponse {
    private Long toonId;

    public CartoonUploadResponse(Long toonId) {
        this.toonId = toonId;
    }

    public CartoonUploadResponse(Cartoon cartoon) {
        this.toonId = cartoon.getId();
    }
}
