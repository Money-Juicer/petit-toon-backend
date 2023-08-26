package com.petit.toon.service.cartoon.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentCreateResponse {

    private long commentId;

    @Builder
    public CommentCreateResponse(long commentId) {
        this.commentId = commentId;
    }
}
