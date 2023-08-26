package com.petit.toon.service.user;

import com.petit.toon.entity.token.RefreshToken;
import com.petit.toon.exception.badrequest.IpAddressNotMatchException;
import com.petit.toon.exception.badrequest.TokenNotValidException;
import com.petit.toon.exception.notfound.RefreshTokenNotFoundException;
import com.petit.toon.repository.token.RefreshTokenRepository;
import com.petit.toon.security.CustomUserDetails;
import com.petit.toon.security.JwtTokenProvider;
import com.petit.toon.service.user.request.LoginServiceRequest;
import com.petit.toon.service.user.request.ReissueServiceRequest;
import com.petit.toon.service.user.response.AuthResponse;
import com.petit.toon.service.user.response.ReissueResponse;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

    public AuthResponse authenticate(LoginServiceRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        long userId = customUserDetails.getUser().getId();
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        saveRefreshToken(authentication, refreshToken, request.getClientIp());
        return AuthResponse.builder()
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public ReissueResponse reissueToken(ReissueServiceRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!(StringUtils.hasText(refreshToken) && jwtTokenProvider.validateToken(refreshToken))) {
            throw new TokenNotValidException();
        }

        RefreshToken findRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(RefreshTokenNotFoundException::new);
        if (!request.getClientIp().equals(findRefreshToken.getIp())) {
            throw new IpAddressNotMatchException();
        }

        String accessToken = jwtTokenProvider.createAccessToken(jwtTokenProvider.getUsername(refreshToken));
        return ReissueResponse.builder()
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
