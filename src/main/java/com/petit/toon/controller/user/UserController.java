package com.petit.toon.controller.user;

import com.petit.toon.controller.user.request.LoginRequest;
import com.petit.toon.controller.user.request.SignupRequest;
import com.petit.toon.service.user.AuthService;
import com.petit.toon.service.user.UserService;
import com.petit.toon.service.user.request.LoginServiceRequest;
import com.petit.toon.service.user.request.ReissueServiceRequest;
import com.petit.toon.service.user.response.AuthResponse;
import com.petit.toon.service.user.response.ReissueResponse;
import com.petit.toon.service.user.response.SignupResponse;
import com.petit.toon.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.petit.toon.security.JwtTokenProvider.ACCESS_TOKEN_VALID_TIME_MILLISECONDS;
import static com.petit.toon.security.JwtTokenProvider.REFRESH_TOKEN_VALID_TIME_MILLISECONDS;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/api/v1/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse httpResponse) {
        LoginServiceRequest serviceRequest = request.toServiceRequest(getClientIp(httpRequest));
        AuthResponse response = authService.authenticate(serviceRequest);
        cookieUtil.add(httpResponse, "accessToken", response.getAccessToken(), (int) ACCESS_TOKEN_VALID_TIME_MILLISECONDS / 1000);
        cookieUtil.add(httpResponse, "refreshToken", response.getRefreshToken(), (int) REFRESH_TOKEN_VALID_TIME_MILLISECONDS / 1000);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(userService.register(request.toServiceRequest()));
    }

    @PostMapping("/api/v1/token/reissue")
    public ResponseEntity<ReissueResponse> reissue(HttpServletRequest httpRequest,
                                                   HttpServletResponse httpResponse) {
        Optional<Cookie> refreshToken = cookieUtil.get(httpRequest, "refreshToken");
        if (refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String tokenValue = refreshToken.get().getValue();
        ReissueServiceRequest serviceRequest = ReissueServiceRequest.builder()
                .refreshToken(tokenValue)
                .clientIp(getClientIp(httpRequest))
                .build();

        ReissueResponse response = authService.reissueToken(serviceRequest);
        cookieUtil.add(httpResponse, "accessToken", response.getAccessToken(), (int) ACCESS_TOKEN_VALID_TIME_MILLISECONDS / 1000);
        return ResponseEntity.ok(response);
    }

    /**
     * Get Client IP Address
     * refs: https://wildeveloperetrain.tistory.com/148
     */
    public String getClientIp(HttpServletRequest request) {
        List<String> headerList = new ArrayList<>();
        headerList.add("X-Forwarded-For");
        headerList.add("HTTP_CLIENT_IP");
        headerList.add("HTTP_X_FORWARDED_FOR");
        headerList.add("HTTP_X_FORWARDED");
        headerList.add("HTTP_FORWARDED_FOR");
        headerList.add("HTTP_FORWARDED");
        headerList.add("Proxy-Client-IP");
        headerList.add("WL-Proxy-Client-IP");
        headerList.add("HTTP_VIA");
        headerList.add("IPV6_ADR");

        for (String header : headerList) {
            String clientIp = request.getHeader(header);
            if (StringUtils.hasText(clientIp) && !clientIp.equals("unknown")) {
                return clientIp;
            }
        }
        return request.getRemoteAddr();
    }
}
