package com.petit.toon.service.collection.response;

import lombok.Getter;

@Getter
public class CollectionResponse {
    private long collectionId;

    public CollectionResponse(long collectionId) {
        this.collectionId = collectionId;
    }
}
