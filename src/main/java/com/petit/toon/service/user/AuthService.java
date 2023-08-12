package com.petit.toon.service.user;

import com.petit.toon.entity.token.RefreshToken;
import com.petit.toon.repository.token.RefreshTokenRepository;
import com.petit.toon.service.user.request.LoginServiceRequest;
import com.petit.toon.service.user.request.ReissueServiceRequest;
import com.petit.toon.service.user.response.AuthResponse;
import com.petit.toon.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

    public AuthResponse authenticate(LoginServiceRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        String accessToken = jwtUtil.createAccessToken(authentication);
        String refreshToken = jwtUtil.createRefreshToken(authentication);

        saveRefreshToken(authentication, refreshToken, request.getClientIp());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse reissueToken(ReissueServiceRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!(StringUtils.hasText(refreshToken) && jwtUtil.validateToken(refreshToken))) {
            throw new RuntimeException("유효하지 않거나 만료된 토큰입니다.");
        }

        RefreshToken findRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found in Redis Cache. token: " + refreshToken));
        if (!request.getClientIp().equals(findRefreshToken.getIp())) {
            throw new RuntimeException("잘못된 접근입니다. 다시 로그인하세요.");
        }

        String accessToken = jwtUtil.createAccessToken(jwtUtil.getUsername(refreshToken));
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveRefreshToken(Authentication authentication, String refreshToken, String clientIp) {
        refreshTokenRepository.save(RefreshToken.builder()
                .id(authentication.getName())
                .ip(clientIp)
                .authorities(authentication.getAuthorities())
                .refreshToken(refreshToken)
                .build());
    }
}
