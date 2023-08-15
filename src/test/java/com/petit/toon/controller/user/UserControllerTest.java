package com.petit.toon.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.controller.user.request.LoginRequest;
import com.petit.toon.controller.user.request.ReissueRequest;
import com.petit.toon.controller.user.request.SignupRequest;
import com.petit.toon.service.user.AuthService;
import com.petit.toon.service.user.UserService;
import com.petit.toon.service.user.response.AuthResponse;
import com.petit.toon.service.user.response.SignupResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ActiveProfiles("test")
class UserControllerTest extends RestDocsSupport {

    @MockBean
    UserService userService;

    @MockBean
    AuthService authService;

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
                                        .description("비밀번호 (8자 이상, 20자 이내, 영어 소문자/대문자/숫자/특수문자(!@#$%^&*~?) 1개 이상 포함)")
                        ),
                        responseFields(
                                fieldWithPath("userId").type(JsonFieldType.NUMBER)
                                        .description("유저 ID")
                        )));
    }

    @Test
    @DisplayName("로그인 API")
    void login() throws Exception {
        // given
        given(authService.authenticate(any())).willReturn(
                AuthResponse.builder()
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
        given(authService.reissueToken(any())).willReturn(
                AuthResponse.builder()
                        .accessToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYW1wbGVAZW1haWwuY29tIiwiZW1haWwiOiJzYW1wbGVAZW1haWwuY29tIiwiaWF0IjoxNjkxODQ5NDU1LCJleHAiOjE2OTE4NTEyNTV9.P_clLb6hZOQ9gHtzhW5-7bFgSbWBaqVnS7AVF-yJ9Qs")
                        .refreshToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYW1wbGVAZW1haWwuY29tIiwiZW1haWwiOiJzYW1wbGVAZW1haWwuY29tIiwiaWF0IjoxNjkxODQ5NDU1LCJleHAiOjE2OTMwNTkwNTV9.mTA7MeINcCshC7Oz5rY6R8RVX8TxrSFgakKBqIhK9pY")
                        .build());

        ReissueRequest request = ReissueRequest.builder()
                .refreshToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYW1wbGVAZW1haWwuY29tIiwiZW1haWwiOiJzYW1wbGVAZW1haWwuY29tIiwiaWF0IjoxNjkxODQ5NDU1LCJleHAiOjE2OTMwNTkwNTV9.mTA7MeINcCshC7Oz5rY6R8RVX8TxrSFgakKBqIhK9pY")
                .build();

        // when // then
        mockMvc.perform(post("/api/v1/token/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("auth-reissue",
                        requestFields(
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                                        .description("refresh 토큰")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").type(JsonFieldType.STRING)
                                        .description("access 토큰 (ttl 30분)"),
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                                        .description("refresh 토큰 (ttl 7일)")
                        )));
    }
}