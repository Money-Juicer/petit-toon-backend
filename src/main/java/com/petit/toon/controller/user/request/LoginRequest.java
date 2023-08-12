package com.petit.toon.controller.user.request;

import com.petit.toon.service.user.request.LoginServiceRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginRequest {

    @Email
    @NotBlank
    private String email;

    @Length(max = 20)
    @NotBlank
    private String password;

    @Builder
    private LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginServiceRequest toServiceRequest(String clientIp) {
        return LoginServiceRequest.builder()
                .email(email)
                .password(password)
                .clientIp(clientIp)
                .build();
    }
}
