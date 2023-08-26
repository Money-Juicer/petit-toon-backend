package com.petit.toon.controller.user;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.FollowService;
import com.petit.toon.service.user.response.UserListResponse;
import com.petit.toon.service.user.response.UserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(H2ConsoleProperties.class)
@WithUserDetails(value = "sample@email.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class FollowControllerTest extends RestDocsSupport {

    @MockBean
    FollowService followService;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(
                User.builder()
                        .email("sample@email.com")
                        .password("SamplePW123!@#")
                        .build());
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("팔로우 등록 API")
    void follow() throws Exception {
        // when // then
        mockMvc.perform(post("/api/v1/follow/{followeeId}", 2L))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("follow-create",
                        pathParameters(
                                parameterWithName("followeeId").description("팔로우할 유저 ID")
                        )
                ));
    }

    @Test
    @DisplayName("팔로우 중복 등록 - DataIntegrityViolationException")
    void follow2() throws Exception {
        // given
        doThrow(new DataIntegrityViolationException("error message"))
                .when(followService).follow(anyLong(), anyLong());

        // when // then
        mockMvc.perform(post("/api/v1/follow/{followeeId}", 2L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("요청 데이터를 처리할 수 없습니다."))
                .andDo(print())
                .andDo(document("exception-data-integrity-violation",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("예외 메시지")
                        )));
    }

    @Test
    @DisplayName("내가 팔로우 하는 유저 목록 조회 API")
    void getFollowingUsers() throws Exception {
        // given
        UserResponse userResponse1 = createUser(1l, "zl존", "@Hotoran");
        UserResponse userResponse2 = createUser(2l, "DrangeWoo", "@timel2ss");

        UserListResponse response = new UserListResponse(List.of(userResponse1, userResponse2));

        given(followService.findFollowingUsers(anyLong(), any())).willReturn(response);

        // when // then
        mockMvc.perform(get("/api/v1/follow/{userId}/following?page=0&size=20", 1l))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].id").value(userResponse1.getId()))
                .andExpect(jsonPath("$.users[1].id").value(userResponse2.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("follow-following-list",
                        pathParameters(
                                parameterWithName("userId").description("유저 ID")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").optional().description("데이터 수")
                        ),
                        responseFields(
                                fieldWithPath("users").type(JsonFieldType.ARRAY)
                                        .description("팔로우 유저 목록"),
                                fieldWithPath("users[].id").type(JsonFieldType.NUMBER)
                                        .description("유저 ID"),
                                fieldWithPath("users[].nickname").type(JsonFieldType.STRING)
                                        .description("유저 닉네임"),
                                fieldWithPath("users[].tag").type(JsonFieldType.STRING)
                                        .description("유저 태그"),
                                fieldWithPath("users[].profileImagePath").type(JsonFieldType.STRING)
                                        .description("유저 프로필 이미지 경로")
                        )
                ));
    }

    @Test
    @DisplayName("나를 팔로우 하는 유저 목록 조회 API")
    void getFollowedUsers() throws Exception {
        // given
        UserResponse userResponse1 = createUser(1l, "zl존", "@Hotoran");
        UserResponse userResponse2 = createUser(2l, "DrangeWoo", "@timel2ss");

        UserListResponse response = new UserListResponse(List.of(userResponse1, userResponse2));

        given(followService.findFollowedUsers(anyLong(), any())).willReturn(response);

        // when // then
        mockMvc.perform(get("/api/v1/follow/{userId}/followed?page=0&size=20", 1l))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].id").value(userResponse1.getId()))
                .andExpect(jsonPath("$.users[1].id").value(userResponse2.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("follow-followed-list",
                        pathParameters(
                                parameterWithName("userId").description("유저 ID")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").optional().description("데이터 수")
                        ),
                        responseFields(
                                fieldWithPath("users").type(JsonFieldType.ARRAY)
                                        .description("팔로우 유저 목록"),
                                fieldWithPath("users[].id").type(JsonFieldType.NUMBER)
                                        .description("유저 ID"),
                                fieldWithPath("users[].nickname").type(JsonFieldType.STRING)
                                        .description("유저 닉네임"),
                                fieldWithPath("users[].tag").type(JsonFieldType.STRING)
                                        .description("유저 태그"),
                                fieldWithPath("users[].profileImagePath").type(JsonFieldType.STRING)
                                        .description("유저 프로필 이미지 경로")
                        )
                ));
    }

    @Test
    @DisplayName("팔로우 삭제 API")
    void unfollow() throws Exception {
        //given // when // then
        mockMvc.perform(delete("/api/v1/follow/{followeeId}", 2L))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("follow-delete",
                        pathParameters(
                                parameterWithName("followeeId").description("팔로우를 취소할 유저 ID")
                        )));
    }

    private UserResponse createUser(long id, String nickname, String tag) {
        return UserResponse.builder()
                .id(id)
                .nickname(nickname)
                .tag(tag)
                .profileImagePath("sample-profile-image-url")
                .build();
    }
}