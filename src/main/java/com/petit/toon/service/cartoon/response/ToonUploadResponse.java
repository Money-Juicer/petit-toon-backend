package com.petit.toon.service.cartoon.response;

import com.petit.toon.entity.cartoon.Cartoon;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToonUploadResponse {
    private Long toonId;

    public ToonUploadResponse(Long toonId) {
        this.toonId = toonId;
    }

    public ToonUploadResponse(Cartoon cartoon) {
        this.toonId = cartoon.getId();
    }
}
