package com.petit.toon.service.cartoon.response;

import com.petit.toon.entity.cartoon.Comment;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MyCommentResponse {
    private long commentId;
    private long cartoonId;
    private String content;

    @Builder
    private MyCommentResponse(long commentId, long cartoonId, String content) {
        this.commentId = commentId;
        this.cartoonId = cartoonId;
        this.content = content;
    }

    public static MyCommentResponse of(Comment comment) {
        return MyCommentResponse.builder()
                .commentId(comment.getId())
                .cartoonId(comment.getCartoon().getId())
                .content(comment.getContent())
                .build();
    }

}
