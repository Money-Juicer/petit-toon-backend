package com.petit.toon.service.user.response;

import lombok.Getter;

@Getter
public class ProfileImageResponse {
    private long profileImageId;

    public ProfileImageResponse(long profileImageId) {
        this.profileImageId = profileImageId;
    }
}
