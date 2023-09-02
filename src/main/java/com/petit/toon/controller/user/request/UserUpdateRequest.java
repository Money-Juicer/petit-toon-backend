package com.petit.toon.controller.user.request;

import com.petit.toon.service.user.request.UserUpdateServiceRequest;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserUpdateRequest {

    @Length(max = 20)
    private String nickname;

    @Pattern(regexp = "^[A-Za-z\\d_.]{1,15}$")
    private String tag;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*~?])[A-Za-z\\d!@#$%^&*~?]{8,20}$")
    private String password;

    @Length(max = 500)
    private String statusMessage;

    @Builder
    private UserUpdateRequest(String nickname, String tag, String password, String statusMessage) {
        this.nickname = nickname;
        this.tag = tag;
        this.password = password;
        this.statusMessage = statusMessage;
    }

    public UserUpdateServiceRequest toServiceRequest(long userId) {
        return UserUpdateServiceRequest.builder()
                .userId(userId)
                .nickname(nickname)
                .tag(tag)
                .password(password)
                .statusMessage(statusMessage)
                .build();
    }
}
