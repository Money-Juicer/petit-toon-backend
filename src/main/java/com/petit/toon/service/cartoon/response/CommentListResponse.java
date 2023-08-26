package com.petit.toon.service.cartoon.response;

import lombok.Getter;

import java.util.List;

@Getter
public class CommentListResponse {

    private List<CommentResponse> comments;

    public CommentListResponse(List<CommentResponse> comments) {
        this.comments = comments;
    }
}
