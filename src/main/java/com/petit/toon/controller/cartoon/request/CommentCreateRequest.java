package com.petit.toon.controller.cartoon.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentCreateRequest {
    @NotBlank
    @Length(max = 200)
    private String content;

    @Builder
    public CommentCreateRequest(String content) {
        this.content = content;
    }
}
