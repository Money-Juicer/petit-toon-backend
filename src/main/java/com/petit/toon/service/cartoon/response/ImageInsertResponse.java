package com.petit.toon.service.cartoon.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageInsertResponse {
    private String path;

    public ImageInsertResponse(String path) {
        this.path = path;
    }
}
