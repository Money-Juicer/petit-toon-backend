package com.petit.toon.service.feed.response;

import lombok.Getter;

import java.util.List;

@Getter
public class FeedResponse {
    private List<Long> feed;

    public FeedResponse(List<Long> feed) {
        this.feed = feed;
    }
}
