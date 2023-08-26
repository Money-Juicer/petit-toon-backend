package com.petit.toon.service.user.response;

import com.petit.toon.entity.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDetailResponse {

    private long id;
    private String nickname;
    private String tag;
    private String profileImagePath;
    private String statusMessage;
    private boolean isFollow;

    @Builder
    private UserDetailResponse(long id, String nickname, String tag,
                               String profileImagePath, String statusMessage, boolean isFollow) {
        this.id = id;
        this.nickname = nickname;
        this.tag = tag;
        this.profileImagePath = profileImagePath;
        this.statusMessage = statusMessage;
        this.isFollow = isFollow;
    }

    public static UserDetailResponse of(User user, boolean isFollow) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .tag(user.getTag())
                .profileImagePath(user.getProfileImage().getPath())
                .statusMessage(user.getStatusMessage())
                .isFollow(isFollow)
                .build();
    }
}
