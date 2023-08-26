package com.petit.toon.service.cartoon.response;

import com.petit.toon.entity.cartoon.Comment;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentResponse {
    private long commentId;
    private long userId;
    private String content;
    private boolean myComment;

    @Builder
    private CommentResponse(long commentId, long userId, String content, boolean myComment) {
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
        this.myComment = myComment;
    }

    public static CommentResponse of(Comment comment, boolean myComment) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .userId(comment.getUser().getId())
                .content(comment.getContent())
                .myComment(myComment)
                .build();
    }

}
