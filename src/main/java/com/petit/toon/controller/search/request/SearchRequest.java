package com.petit.toon.controller.search.request;

import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchRequest {

    @Pattern(regexp = "^[a-zA-Z0-9가-힣\\s]+$")
    private String keyword;

    public SearchRequest(String keyword) {
        this.keyword = keyword;
    }
}
