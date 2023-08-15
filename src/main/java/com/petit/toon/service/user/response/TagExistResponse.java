package com.petit.toon.service.user.response;

import lombok.Getter;

@Getter
public class TagExistResponse {
    private boolean tagExist;

    public TagExistResponse(boolean tagExist) {
        this.tagExist = tagExist;
    }
}

