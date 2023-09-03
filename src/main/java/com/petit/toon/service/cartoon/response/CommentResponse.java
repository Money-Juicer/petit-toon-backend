package com.petit.toon.service.cartoon.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.petit.toon.entity.cartoon.Comment;
import com.petit.toon.service.user.response.UserResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {
    private long commentId;
    private UserResponse userInfo;
    private String content;
    private boolean myComment;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdDateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime modifiedDateTime;

    @Builder
    private CommentResponse(long commentId, UserResponse userInfo, String content, boolean myComment,
                            LocalDateTime createdDateTime, LocalDateTime modifiedDateTime) {
        this.commentId = commentId;
        this.userInfo = userInfo;
        this.content = content;
        this.myComment = myComment;
        this.createdDateTime = createdDateTime;
        this.modifiedDateTime = modifiedDateTime;
    }

    public static CommentResponse of(Comment comment, boolean myComment) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .userInfo(UserResponse.of(comment.getUser()))
                .content(comment.getContent())
                .myComment(myComment)
                .createdDateTime(comment.getCreatedDateTime())
                .modifiedDateTime(comment.getModifiedDateTime())
                .build();
    }

}
