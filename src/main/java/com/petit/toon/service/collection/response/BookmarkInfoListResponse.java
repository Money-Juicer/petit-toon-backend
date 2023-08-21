package com.petit.toon.service.collection.response;

import lombok.Getter;

import java.util.List;

@Getter
public class BookmarkInfoListResponse {
    private List<BookmarkInfoResponse> bookmarkInfos;

    public BookmarkInfoListResponse(List<BookmarkInfoResponse> bookmarkInfos) {
        this.bookmarkInfos = bookmarkInfos;
    }
}
