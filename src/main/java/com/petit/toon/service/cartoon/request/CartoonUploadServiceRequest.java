package com.petit.toon.service.cartoon.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartoonUploadServiceRequest {
    private String title;
    private String description;
    private List<MultipartFile> toonImages;

    @Builder
    public CartoonUploadServiceRequest(String title, String description,
                                       List<MultipartFile> toonImages) {
        this.title = title;
        this.description = description;
        this.toonImages = toonImages;
    }

}
