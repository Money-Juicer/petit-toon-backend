package com.petit.toon.service.cartoon.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.petit.toon.entity.cartoon.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyCommentResponse {
    private long commentId;
    private long cartoonId;
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdDateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime modifiedDateTime;

    @Builder
    private MyCommentResponse(long commentId, long cartoonId, String content,
                              LocalDateTime createdDateTime, LocalDateTime modifiedDateTime) {
        this.commentId = commentId;
        this.cartoonId = cartoonId;
        this.content = content;
        this.createdDateTime = createdDateTime;
        this.modifiedDateTime = modifiedDateTime;
    }

    public static MyCommentResponse of(Comment comment) {
        return MyCommentResponse.builder()
                .commentId(comment.getId())
                .cartoonId(comment.getCartoon().getId())
                .content(comment.getContent())
                .createdDateTime(comment.getCreatedDateTime())
                .modifiedDateTime(comment.getModifiedDateTime())
                .build();
    }
}
