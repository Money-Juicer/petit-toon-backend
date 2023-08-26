package com.petit.toon.service.cartoon.response;

import lombok.Getter;

import java.util.List;

@Getter
public class MyCommentListResponse {
    private List<MyCommentResponse> comments;

    public MyCommentListResponse(List<MyCommentResponse> comments) {
        this.comments = comments;
    }
}
