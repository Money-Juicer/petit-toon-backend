package com.petit.toon.service.collection.response;

import com.petit.toon.entity.collection.Collection;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CollectionInfoResponse {

    private long id;
    private String title;
    private boolean closed;

    @Builder
    private CollectionInfoResponse(long id, String title, boolean closed) {
        this.id = id;
        this.title = title;
        this.closed = closed;
    }

    public static CollectionInfoResponse of(Collection collection) {
        return CollectionInfoResponse.builder()
                .id(collection.getId())
                .title(collection.getTitle())
                .closed(collection.isClosed())
                .build();
    }

}
