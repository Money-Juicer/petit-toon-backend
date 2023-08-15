package com.petit.toon.controller.user;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.service.user.InquiryService;
import com.petit.toon.service.user.response.TagExistResponse;
import com.petit.toon.service.user.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(controllers = InquiryController.class)
@ActiveProfiles("test")
class InquiryControllerTest extends RestDocsSupport {

    @MockBean
    InquiryService inquiryService;

    @Test
    @DisplayName("유저 정보 조회 API")
    void inquiry() throws Exception {
        // given
        given(inquiryService.inquiryByUserId(anyLong())).willReturn(UserResponse.builder()
                .id(1l)
                .nickname("sample-nickname")
                .tag("sample-tag")
                .profileImagePath("sample-path")
                .statusMessage("sample-message")
                .build());

        // when // then
        mockMvc.perform(get("/api/v1/user/{userId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1l))
                .andExpect(jsonPath("$.nickname").value("sample-nickname"))
                .andExpect(jsonPath("$.tag").value("sample-tag"))
                .andExpect(jsonPath("$.profileImagePath").value("sample-path"))
                .andExpect(jsonPath("$.statusMessage").value("sample-message"))
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("user-inquiry",
                        pathParameters(
                                parameterWithName("userId").description("유저 ID")
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
                                        .description("조회된 유저 상태메시지")
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