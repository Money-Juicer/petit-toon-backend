package com.petit.toon.controller.user.request;

import com.petit.toon.service.user.request.SignupServiceRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupRequest {

    @NotBlank
    @Length(max = 20)
    private String name;

    @NotBlank
    @Length(max = 20)
    private String nickname;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z\\d_.]{1,15}$")
    private String tag;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*~?])[A-Za-z\\d!@#$%^&*~?]{8,20}$")
    private String password;

    @Builder
    private SignupRequest(String name, String nickname, String tag, String email, String password) {
        this.name = name;
        this.nickname = nickname;
        this.tag = tag;
        this.email = email;
        this.password = password;
    }

    public SignupServiceRequest toServiceRequest() {
        return SignupServiceRequest.builder()
                .name(name)
                .nickname(nickname)
                .tag(tag)
                .email(email)
                .password(password)
                .build();
    }
}
