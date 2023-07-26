package com.petit.toon.controller.cartoon.dto.request;

import com.petit.toon.service.cartoon.dto.input.ToonUploadInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToonUploadRequest {
    @Positive
    private Long userId;
    @NotBlank
    private String title;
    private String description;
    @NotNull
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
