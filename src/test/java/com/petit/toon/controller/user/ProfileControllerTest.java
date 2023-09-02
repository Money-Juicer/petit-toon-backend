package com.petit.toon.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.controller.user.request.UserUpdateRequest;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.ProfileImageService;
import com.petit.toon.service.user.UserService;
import com.petit.toon.service.user.response.ProfileImageResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableConfigurationProperties(H2ConsoleProperties.class)
@WithUserDetails(value = "sample@email.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@ActiveProfiles("test")
class ProfileControllerTest extends RestDocsSupport {

    @MockBean
    ProfileImageService profileImageService;

    @MockBean
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    String absolutePath;

    @BeforeEach
    void setUp() {
        String path = "src/test/resources/sample-profile-images";
        absolutePath = new File(path).getAbsolutePath();

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
    @DisplayName("프로필 이미지 등록")
    void uploadProfileImage() throws Exception {
        //given
        given(profileImageService.upload(anyLong(), any())).willReturn(new ProfileImageResponse(1l));
        MockMultipartFile file1 = new MockMultipartFile("profileImage", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        //when, then
        mockMvc.perform(multipart("/api/v1/user/image/upload")
                .file(file1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileImageId").value(1l))
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("profile-image-upload",
                        requestParts(
                                partWithName("profileImage").description("프로필 이미지 파일")
                        ),
                        responseFields(
                                fieldWithPath("profileImageId").description("업로드된 프로필 이미지 ID")
                        )
                ));
    }

    @Test
    @DisplayName("Default 프로필 이미지 전환")
    void updateToDefault() throws Exception {
        //given // when // then
        mockMvc.perform(patch("/api/v1/user/image/default"))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("profile-image-default"));
    }

    @Test
    @DisplayName("유저 프로필 정보 변경 API")
    void profileUpdate() throws Exception {
        // given
        UserUpdateRequest request = UserUpdateRequest.builder()
                .nickname("김영현")
                .tag("Kingggg")
                .password("q1w2e3r4!")
                .statusMessage("")
                .build();

        // when // then
        mockMvc.perform(patch("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("profile-update",
                        requestFields(
                                fieldWithPath("nickname").type(JsonFieldType.STRING)
                                        .description("변경할 닉네임 (20자 이내)"),
                                fieldWithPath("tag").type(JsonFieldType.STRING)
                                        .description("변경할 태그 (15자 이내, 영문, 특수문자(_.) 허용)"),
                                fieldWithPath("password").type(JsonFieldType.STRING)
                                        .description("변경할 비밀번호 (8자 이상, 20자 이내, 영어 소문자/숫자/특수문자(!@#$%^&*~?) 1개 이상 포함)"),
                                fieldWithPath("statusMessage").type(JsonFieldType.STRING)
                                        .description("변경할 상태 메시지 (500자 이내)")
                        )));
    }

    @Test
    @DisplayName("유저 프로필 정보 변경 API - null 필드 허용")
    void profileUpdate2() throws Exception {
        // given
        UserUpdateRequest request = UserUpdateRequest.builder()
                .tag("Kingggg")
                .statusMessage("statusMessage")
                .build();

        // when // then
        mockMvc.perform(patch("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }
}