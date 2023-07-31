package com.petit.toon.controller.user;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.service.user.ProfileImageService;
import com.petit.toon.service.user.response.ProfileImageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.io.File;
import java.io.FileInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfileController.class)
@ActiveProfiles("test")
class ProfileControllerTest extends RestDocsSupport {

    @MockBean
    ProfileImageService profileImageService;

    String absolutePath;
    @BeforeEach
    void setUp() {
        String path = "src/test/resources/sample-profile-images";
        absolutePath = new File(path).getAbsolutePath();
    }

    @Test
    @DisplayName("프로필 이미지 등록")
    void uploadProfileImage() throws Exception {
        //given
        given(profileImageService.upload(anyLong(), any())).willReturn(new ProfileImageResponse(1l));
        MockMultipartFile file1 = new MockMultipartFile("profileImage", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        //when, then
        mockMvc.perform(multipart("/api/v1/user/{userId}/image/upload", 1)
                .file(file1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileImageId").value(1l))
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("profileImage-upload",
                        pathParameters(
                                parameterWithName("userId").description("유저 ID")
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
        mockMvc.perform(delete("/api/v1/user/{userId}/image/default", 1))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("profileImage-delete",
                        pathParameters(
                                parameterWithName("userId").description("유저 ID")
                        )));
    }
}