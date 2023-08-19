package com.petit.toon.service.user;

import com.petit.toon.entity.token.RefreshToken;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.token.RefreshTokenRepository;
import com.petit.toon.security.CustomUserDetails;
import com.petit.toon.service.user.request.LoginServiceRequest;
import com.petit.toon.service.user.request.ReissueServiceRequest;
import com.petit.toon.service.user.response.AuthResponse;
import com.petit.toon.security.JwtTokenProvider;
import com.petit.toon.util.RedisUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.doReturn;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    AuthService authService;

    @MockBean
    AuthenticationManager authenticationManager;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @SpyBean
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    RedisUtil redisUtil;

    @AfterEach
    void tearDown() {
        redisUtil.flushAll();
    }

    @Test
    @DisplayName("로그인을 하면 access token과 refresh token이 response로 반환된다.")
    void authenticate() {
        // given
        User user = User.builder()
                .email("sample@email.com")
                .password("12345")
                .build();
        given(authenticationManager.authenticate(any()))
                .willReturn(new TestingAuthenticationToken(new CustomUserDetails(user), "test"));

        LoginServiceRequest request = LoginServiceRequest.builder()
                .email("sample@email.com")
                .password("12345")
                .clientIp("128.0.0.1")
                .build();

        // when
        AuthResponse response = authService.authenticate(request);

        // then
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(response.getRefreshToken()).get();
        assertThat(refreshToken.getIp()).isEqualTo(request.getClientIp());
        assertThat(refreshToken.getRefreshToken()).isEqualTo(response.getRefreshToken());
    }

    @Test
    @DisplayName("refresh token이 유효하면 access token을 재발급한다")
    void reissue() {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .ip("128.0.0.1")
                .refreshToken("refreshTokenData")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        refreshTokenRepository.save(refreshToken);

        doReturn(true)
                .when(jwtTokenProvider)
                .validateToken(anyString());
        doReturn("sample@email.com")
                .when(jwtTokenProvider)
                .getUsername(anyString());
        doReturn("accessTokenData")
                .when(jwtTokenProvider)
                .createAccessToken(anyString());

        ReissueServiceRequest request = ReissueServiceRequest.builder()
                .refreshToken("refreshTokenData")
                .clientIp("128.0.0.1")
                .build();

        // when
        AuthResponse response = authService.reissueToken(request);

        // then
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("refresh token이 유효하지 않으면 예외가 발생한다")
    void reissue2() {
        // given
        ReissueServiceRequest request = ReissueServiceRequest.builder()
                .refreshToken("refreshTokenData")
                .clientIp("128.0.0.1")
                .build();

        // when // then
        assertThatThrownBy(() -> authService.reissueToken(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("유효하지 않거나 만료된 토큰입니다.");
    }

    @Test
    @DisplayName("로그인한 ip 주소와 request의 ip 주소가 다르면 예외가 발생한다.")
    void reissue3() {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .ip("128.0.0.1")
                .refreshToken("refreshTokenData")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        refreshTokenRepository.save(refreshToken);

        doReturn(true)
                .when(jwtTokenProvider)
                .validateToken(anyString());

        ReissueServiceRequest request = ReissueServiceRequest.builder()
                .refreshToken("refreshTokenData")
                .clientIp("192.168.0.1")
                .build();

        // when // then
        assertThatThrownBy(() -> authService.reissueToken(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("잘못된 접근입니다. 다시 로그인하세요.");
    }
}