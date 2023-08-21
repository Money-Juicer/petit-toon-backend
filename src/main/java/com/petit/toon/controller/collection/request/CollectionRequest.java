package com.petit.toon.controller.collection.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CollectionRequest {

    @NotBlank
    private String title;
    private boolean closed;

    @Builder
    private CollectionRequest(String title, boolean closed) {
        this.title = title;
        this.closed = closed;
    }
}
