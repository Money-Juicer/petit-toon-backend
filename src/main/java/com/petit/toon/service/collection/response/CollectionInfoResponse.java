package com.petit.toon.service.collection.response;

import com.petit.toon.entity.collection.Collection;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CollectionInfoResponse {

    private long id;
    private String title;
    private boolean closed;
    private List<BookmarkInfoResponse> bookmarkInfos;

    @Builder
    private CollectionInfoResponse(long id, String title, boolean closed, List<BookmarkInfoResponse> bookmarkInfos) {
        this.id = id;
        this.title = title;
        this.closed = closed;
        this.bookmarkInfos = bookmarkInfos;
    }

    public static CollectionInfoResponse of(Collection collection) {
        return CollectionInfoResponse.builder()
                .id(collection.getId())
                .title(collection.getTitle())
                .closed(collection.isClosed())
                .bookmarkInfos(collection.getBookmarks()
                        .subList(0, Math.min(3, collection.getBookmarks().size()))
                        .stream().map(BookmarkInfoResponse::of)
                        .toList())
                .build();
    }
}
