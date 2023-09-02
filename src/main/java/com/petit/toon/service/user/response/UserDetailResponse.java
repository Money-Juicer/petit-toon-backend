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
    private long followerCount;
    private long followCount;

    @Builder
    private UserDetailResponse(long id, String nickname, String tag,
                               String profileImagePath, String statusMessage, boolean isFollow,
                               long followerCount, long followCount) {
        this.id = id;
        this.nickname = nickname;
        this.tag = tag;
        this.profileImagePath = profileImagePath;
        this.statusMessage = statusMessage;
        this.isFollow = isFollow;
        this.followerCount = followerCount;
        this.followCount = followCount;
    }

    public static UserDetailResponse of(User user, boolean isFollow, long followerCount, long followCount) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .tag(user.getTag())
                .profileImagePath(user.getProfileImage().getPath())
                .statusMessage(user.getStatusMessage())
                .isFollow(isFollow)
                .followerCount(followerCount)
                .followCount(followCount)
                .build();
    }
}
