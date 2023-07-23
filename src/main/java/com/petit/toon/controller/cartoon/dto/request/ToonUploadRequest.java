package com.petit.toon.controller.cartoon.dto.request;

import com.petit.toon.service.cartoon.dto.input.ToonUploadInput;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToonUploadRequest {
    private Long userId;
    private String title;
    private String description;
    private List<MultipartFile> toonImages;

    @Builder
    public ToonUploadRequest(Long userId, String title, String description,
                             List<MultipartFile> toonImages) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.toonImages = toonImages;
    }

    public ToonUploadInput toInput() {
        return ToonUploadInput.builder()
                .userId(userId)
                .title(title)
                .description(description)
                .toonImages(toonImages)
                .build();
    }
}
