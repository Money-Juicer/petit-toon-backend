package com.petit.toon.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.controller.user.request.LoginRequest;
import com.petit.toon.controller.user.request.SignupRequest;
import com.petit.toon.exception.badrequest.EmailAlreadyRegisteredException;
import com.petit.toon.exception.badrequest.IpAddressNotMatchException;
import com.petit.toon.exception.badrequest.TokenNotValidException;
import com.petit.toon.exception.internalservererror.AuthorityNotExistException;
import com.petit.toon.exception.notfound.RefreshTokenNotFoundException;
import com.petit.toon.service.user.AuthService;
import com.petit.toon.service.user.UserService;
import com.petit.toon.service.user.response.AuthResponse;
import com.petit.toon.service.user.response.ReissueResponse;
import com.petit.toon.service.user.response.SignupResponse;
import com.petit.toon.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ActiveProfiles("test")
class UserControllerTest extends RestDocsSupport {

    @MockBean
    UserService userService;

    @MockBean
    AuthService authService;

    @Autowired
    CookieUtil cookieUtil;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 API")
    void signup() throws Exception {
        // given
        given(userService.register(any()))
                .willReturn(new SignupResponse(1L));

        SignupRequest request = SignupRequest.builder()
                .name("sample_name")
                .nickname("sample_nickname")
                .tag("sample_tag")
                .email("sample@email.com")
                .password("!@#sAmple1234")
                .build();

        // when // then
        mockMvc.perform(post("/api/v1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user-signup",
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("이름 (20자 이내)"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING)
                                        .description("닉네임 (20자 이내)"),
                                fieldWithPath("tag").type(JsonFieldType.STRING)
                                        .description("태그 (15자 이내, 영문, 특수문자(_.) 허용"),
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일 주소 (이메일 형식)"),
                                fieldWithPath("password").type(JsonFieldType.STRING)
                                        .description("비밀번호 (8자 이상, 20자 이내, 영어 소문자/숫자/특수문자(!@#$%^&*~?) 1개 이상 포함)")
                        ),
                        responseFields(
                                fieldWithPath("userId").type(JsonFieldType.NUMBER)
                                        .description("유저 ID")
                        )));
    }

    @Test
    @DisplayName("회원가입 API - EmailAlreadyRegistered")
    void signup2() throws Exception {
        // given
        given(userService.register(any())).willThrow(new EmailAlreadyRegisteredException());

        SignupRequest request = SignupRequest.builder()
                .name("이미 등록된 이메일 주소")
                .nickname("sample_nickname")
                .tag("sample_tag")
                .email("sample@email.com")
                .password("!@#sAmple1234")
                .build();

        // when // then
        mockMvc.perform(post("/api/v1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value(EmailAlreadyRegisteredException.MESSAGE))
                .andDo(print())
                .andDo(document("exception-email-already-registered",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("예외 메시지")
                        )));
    }

    @Test
    @DisplayName("회원가입 API - ServerError")
    void signup3() throws Exception {
        // given
        given(userService.register(any())).willThrow(new AuthorityNotExistException());

        SignupRequest request = SignupRequest.builder()
                .name("sample_name")
                .nickname("sample_nickname")
                .tag("sample_tag")
                .email("sample@email.com")
                .password("!@#sAmple1234")
                .build();

        // when // then
        mockMvc.perform(post("/api/v1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.message").value(AuthorityNotExistException.MESSAGE))
                .andDo(print())
                .andDo(document("exception-server-error",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("예외 메시지")
                        )));
    }

    @Test
    @DisplayName("회원가입 API - MethodArgumentNotValid")
    void signup4() throws Exception {
        // given
        SignupRequest request = SignupRequest.builder()
                .name("sample_name")
                .nickname("sample_nickname")
                .tag("sample_tag")
                .email("이메일 형식이 아님")
                .password("비밀번호 조건을 달성하지 못함")
                .build();

        // when // then
        mockMvc.perform(post("/api/v1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andDo(print())
                .andDo(document("exception-argument-not-valid",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("예외 메시지"),
                                fieldWithPath("validation").type(JsonFieldType.OBJECT)
                                        .description("검증 에러 메시지"),
                                fieldWithPath("validation.password").type(JsonFieldType.STRING)
                                        .description("검증이 실패한 항목/이유"),
                                fieldWithPath("validation.email").type(JsonFieldType.STRING)
                                        .description("검증이 실패한 항목/이유")
                        )));
    }

    @Test
    @DisplayName("로그인 API")
    void login() throws Exception {
        // given
        given(authService.authenticate(any())).willReturn(
                AuthResponse.builder()
                        .userId(1L)
                        .accessToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYW1wbGVAZW1haWwuY29tIiwiZW1haWwiOiJzYW1wbGVAZW1haWwuY29tIiwiaWF0IjoxNjkxODQ5NDU1LCJleHAiOjE2OTE4NTEyNTV9.P_clLb6hZOQ9gHtzhW5-7bFgSbWBaqVnS7AVF-yJ9Qs")
                        .refreshToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYW1wbGVAZW1haWwuY29tIiwiZW1haWwiOiJzYW1wbGVAZW1haWwuY29tIiwiaWF0IjoxNjkxODQ5NDU1LCJleHAiOjE2OTMwNTkwNTV9.mTA7MeINcCshC7Oz5rY6R8RVX8TxrSFgakKBqIhK9pY")
                        .build());

        LoginRequest request = LoginRequest.builder()
                .email("sample@email.com")
                .password("q1w2e3r4!@#")
                .build();

        // when // then
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("auth-login",
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("이메일 주소 (이메일 형식)"),
                                fieldWithPath("password").type(JsonFieldType.STRING)
                                        .description("비밀번호 (20자 이내)")
                        ),
                        responseFields(
                                fieldWithPath("userId").type(JsonFieldType.NUMBER)
                                        .description("유저 ID"),
                                fieldWithPath("accessToken").type(JsonFieldType.STRING)
                                        .description("access 토큰 (ttl 30분)"),
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                                        .description("refresh 토큰 (ttl 7일)")
                        )));
    }

    @Test
    @DisplayName("토큰 재발행 API")
    void reissue() throws Exception {
        // given
        Cookie cookie = new Cookie("refreshToken", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYW1wbGVAZW1haWwuY29tIiwiZW1haWwiOiJzYW1wbGVAZW1haWwuY29tIiwiaWF0IjoxNjkxODQ5NDU1LCJleHAiOjE2OTMwNTkwNTV9.mTA7MeINcCshC7Oz5rY6R8RVX8TxrSFgakKBqIhK9pY");
        given(cookieUtil.get(any(), anyString())).willReturn(Optional.of(cookie));
        given(authService.reissueToken(any())).willReturn(
                ReissueResponse.builder()
                        .accessToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYW1wbGVAZW1haWwuY29tIiwiZW1haWwiOiJzYW1wbGVAZW1haWwuY29tIiwiaWF0IjoxNjkxODQ5NDU1LCJleHAiOjE2OTE4NTEyNTV9.P_clLb6hZOQ9gHtzhW5-7bFgSbWBaqVnS7AVF-yJ9Qs")
                        .refreshToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYW1wbGVAZW1haWwuY29tIiwiZW1haWwiOiJzYW1wbGVAZW1haWwuY29tIiwiaWF0IjoxNjkxODQ5NDU1LCJleHAiOjE2OTMwNTkwNTV9.mTA7MeINcCshC7Oz5rY6R8RVX8TxrSFgakKBqIhK9pY")
                        .build());

        mockMvc.perform(post("/api/v1/token/reissue")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("auth-reissue",
                        requestCookies(
                                cookieWithName("refreshToken").description("refresh 토큰")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").type(JsonFieldType.STRING)
                                        .description("access 토큰 (ttl 30분)"),
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                                        .description("refresh 토큰 (ttl 7일)")
                        )));
    }

    @Test
    @DisplayName("토큰 재발행 API - TokenNotValid")
    void reissue2() throws Exception {
        // given
        Cookie cookie = new Cookie("refreshToken", "잘못된 토큰 값을 입력한 경우");
        given(cookieUtil.get(any(), anyString())).willReturn(Optional.of(cookie));
        given(authService.reissueToken(any())).willThrow(new TokenNotValidException());

        // when // then
        mockMvc.perform(post("/api/v1/token/reissue")
                        .cookie(cookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value(TokenNotValidException.MESSAGE))
                .andDo(print())
                .andDo(document("exception-token-not-valid",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("예외 메시지")
                        )));
    }

    @Test
    @DisplayName("토큰 재발행 API - TokenNotFound")
    void reissue3() throws Exception {
        // given
        Cookie cookie = new Cookie("refreshToken", "Refresh 토큰 유효 기간이 지난 경우");
        given(cookieUtil.get(any(), anyString())).willReturn(Optional.of(cookie));
        given(authService.reissueToken(any())).willThrow(new RefreshTokenNotFoundException());

        // when // then
        mockMvc.perform(post("/api/v1/token/reissue")
                        .cookie(cookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value(RefreshTokenNotFoundException.MESSAGE))
                .andDo(print())
                .andDo(document("exception-token-not-found",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("예외 메시지")
                        )));
    }

    @Test
    @DisplayName("토큰 재발행 API - IpAddressNotMatch")
    void reissue4() throws Exception {
        // given
        Cookie cookie = new Cookie("refreshToken", "로그인 했을 때의 IP 주소와 다른 경우");
        given(cookieUtil.get(any(), anyString())).willReturn(Optional.of(cookie));
        given(authService.reissueToken(any())).willThrow(new IpAddressNotMatchException());

        // when // then
        mockMvc.perform(post("/api/v1/token/reissue")
                        .cookie(cookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value(IpAddressNotMatchException.MESSAGE))
                .andDo(print())
                .andDo(document("exception-ip-not-match",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("예외 메시지")
                        )));
    }
}