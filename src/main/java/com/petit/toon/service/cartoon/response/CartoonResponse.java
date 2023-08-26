package com.petit.toon.service.cartoon.response;

import com.petit.toon.entity.cartoon.Cartoon;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartoonResponse {
    private long id;
    private String title;
    private String author;
    private String thumbnailUrl;

    @Builder
    private CartoonResponse(long id, String title, String author, String thumbnailUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static CartoonResponse of(Cartoon cartoon) {
        return CartoonResponse.builder()
                .id(cartoon.getId())
                .title(cartoon.getTitle())
                .author(cartoon.getUser().getNickname())
                .thumbnailUrl(cartoon.getThumbnailPath())
                .build();
    }
}
