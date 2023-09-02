package com.petit.toon.controller.user;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.notfound.UserNotFoundException;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.InquiryService;
import com.petit.toon.service.user.response.TagExistResponse;
import com.petit.toon.service.user.response.UserDetailResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(H2ConsoleProperties.class)
@WithUserDetails(value = "sample@email.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class InquiryControllerTest extends RestDocsSupport {

    @MockBean
    InquiryService inquiryService;

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
    @DisplayName("유저 정보 조회 API")
    void inquiry() throws Exception {
        // given
        given(inquiryService.inquiryByUserId(anyLong(), anyLong())).willReturn(UserDetailResponse.builder()
                .id(1l)
                .nickname("sample-nickname")
                .tag("sample-tag")
                .profileImagePath("sample-path")
                .statusMessage("sample-message")
                .isFollow(true)
                .followerCount(20L)
                .followCount(42L)
                .build());

        // when // then
        mockMvc.perform(get("/api/v1/user/{inquiryUserId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1l))
                .andExpect(jsonPath("$.nickname").value("sample-nickname"))
                .andExpect(jsonPath("$.tag").value("sample-tag"))
                .andExpect(jsonPath("$.profileImagePath").value("sample-path"))
                .andExpect(jsonPath("$.statusMessage").value("sample-message"))
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("user-inquiry",
                        pathParameters(
                                parameterWithName("inquiryUserId").description("조회할 유저 ID")
                        ),
                        responseFields(
                                fieldWithPath("id")
                                        .description("조회된 유저 ID"),
                                fieldWithPath("nickname")
                                        .description("조회된 유저 닉네임"),
                                fieldWithPath("tag")
                                        .description("조회된 유저 태그"),
                                fieldWithPath("profileImagePath")
                                        .description("조회된 유저 프로필 이미지 경로"),
                                fieldWithPath("statusMessage")
                                        .description("조회된 유저 상태메시지"),
                                fieldWithPath("follow").type(JsonFieldType.BOOLEAN)
                                        .description("팔로우 여부"),
                                fieldWithPath("followerCount").type(JsonFieldType.NUMBER)
                                        .description("나를 팔로우 하는 유저 수"),
                                fieldWithPath("followCount").type(JsonFieldType.NUMBER)
                                        .description("내가 팔로우 하는 유저 수")
                        )
                ));
    }

    @Test
    @DisplayName("유저 정보 조회 API - UserNotFound")
    void inquiry2() throws Exception {
        // given
        given(inquiryService.inquiryByUserId(anyLong(), anyLong())).willThrow(new UserNotFoundException());

        // when // then
        mockMvc.perform(get("/api/v1/user/{inquiryUserId}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value(UserNotFoundException.MESSAGE))
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("exception-user-not-found",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("예외 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("Tag 중복 조회 API")
    void checkDuplicateTag() throws Exception {
        // given
        given(inquiryService.checkTagExist(anyString())).willReturn(new TagExistResponse(anyBoolean()));

        // when // then
        mockMvc.perform(post("/api/v1/user/{tag}", "sample-tag"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("tag-duplication-check",
                        pathParameters(
                                parameterWithName("tag").description("유저 태그")
                        ),
                        responseFields(
                                fieldWithPath("tagExist").description("태그 존재 여부")
                        )

                ));
    }
}