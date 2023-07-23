package com.petit.toon.service.cartoon.dto.input;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToonUploadInput {
    private Long userId;
    private String title;
    private String description;
    private List<MultipartFile> toonImages;

    @Builder
    public ToonUploadInput(Long userId, String title, String description,
                           List<MultipartFile> toonImages) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.toonImages = toonImages;
    }

}
