package com.petit.toon.controller.cartoon.request;

import com.petit.toon.service.cartoon.request.CartoonUploadServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartoonUploadRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private List<MultipartFile> toonImages;

    @Builder
    public CartoonUploadRequest(String title, String description,
                                List<MultipartFile> toonImages) {
        this.title = title;
        this.description = description;
        this.toonImages = toonImages;
    }

    public CartoonUploadServiceRequest toInput() {
        return CartoonUploadServiceRequest.builder()
                .title(title)
                .description(description)
                .toonImages(toonImages)
                .build();
    }
}
