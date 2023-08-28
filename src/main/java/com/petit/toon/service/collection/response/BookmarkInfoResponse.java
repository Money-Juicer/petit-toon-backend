package com.petit.toon.service.collection.response;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.collection.Bookmark;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BookmarkInfoResponse {
    private long bookmarkId;
    private long cartoonId;
    private String cartoonTitle;
    private String thumbnailPath;

    @Builder
    private BookmarkInfoResponse(long bookmarkId, long cartoonId, String cartoonTitle, String thumbnailPath) {
        this.bookmarkId = bookmarkId;
        this.cartoonId = cartoonId;
        this.cartoonTitle = cartoonTitle;
        this.thumbnailPath = thumbnailPath;
    }

    public static BookmarkInfoResponse of(Bookmark bookmark) {
        Cartoon tmpCartoon = bookmark.getCartoon();
        return BookmarkInfoResponse.builder()
                .bookmarkId(bookmark.getId())
                .cartoonId(tmpCartoon.getId())
                .cartoonTitle(tmpCartoon.getTitle())
                .thumbnailPath(tmpCartoon.getThumbnailPath())
                .build();
    }
}
