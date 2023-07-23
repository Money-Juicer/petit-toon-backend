package com.petit.toon.controller.cartoon.dto.response;
public class ToonUploadResponse {
    private Long toonId;

    public Long getToonId() {
        return toonId;
    }

    public ToonUploadResponse (Long output) {
        this.toonId = output;
    }
}
