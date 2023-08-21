package com.petit.toon.service.collection.response;

import lombok.Getter;

@Getter
public class BookmarkResponse {
    private long bookmarkId;

    public BookmarkResponse(long bookmarkId) {
        this.bookmarkId = bookmarkId;
    }
}
