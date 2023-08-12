package com.petit.toon.controller.user;

import com.petit.toon.controller.user.request.LoginRequest;
import com.petit.toon.controller.user.request.ReissueRequest;
import com.petit.toon.controller.user.request.SignupRequest;
import com.petit.toon.service.user.AuthService;
import com.petit.toon.service.user.UserService;
import com.petit.toon.service.user.request.LoginServiceRequest;
import com.petit.toon.service.user.request.ReissueServiceRequest;
import com.petit.toon.service.user.response.AuthResponse;
import com.petit.toon.service.user.response.SignupResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/api/v1/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        LoginServiceRequest serviceRequest = request.toServiceRequest(getClientIp(httpRequest));
        return ResponseEntity.ok(authService.authenticate(serviceRequest));
    }

    @PostMapping("/api/v1/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(userService.register(request.toServiceRequest()));
    }

    @PostMapping("/api/v1/token/reissue")
    public ResponseEntity<AuthResponse> reissue(@Valid @RequestBody ReissueRequest request,
                                                HttpServletRequest httpRequest) {
        ReissueServiceRequest serviceRequest = request.toServiceRequest(getClientIp(httpRequest));
        return ResponseEntity.ok(authService.reissueToken(serviceRequest));
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
                return request.getRemoteAddr();
            }
        }
        return null;
    }
}
