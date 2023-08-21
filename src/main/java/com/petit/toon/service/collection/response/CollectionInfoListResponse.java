package com.petit.toon.service.collection.response;

import lombok.Getter;

import java.util.List;

@Getter
public class CollectionInfoListResponse {
    private List<CollectionInfoResponse> collectionInfos;

    public CollectionInfoListResponse(List<CollectionInfoResponse> collectionInfos) {
        this.collectionInfos = collectionInfos;
    }
}
