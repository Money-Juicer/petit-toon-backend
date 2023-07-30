package com.petit.toon.service.cartoon.response;

import com.petit.toon.entity.cartoon.Cartoon;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CartoonResponse {
    private long id;
    private String title;
    private String description;
    private String author;
    private String thumbnailUrl;

    @Builder
    private CartoonResponse(long id, String title, String description, String author, String thumbnailUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static CartoonResponse of(Cartoon cartoon) {
        return CartoonResponse.builder()
                .id(cartoon.getId())
                .title(cartoon.getTitle())
                .description(cartoon.getDescription())
                .author(cartoon.getUser().getNickname())
                .thumbnailUrl(cartoon.getThumbnailPath())
                .build();
    }
}
