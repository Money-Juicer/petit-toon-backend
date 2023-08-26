package com.petit.toon.controller.user;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.ProfileImageService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.io.File;
import java.io.FileInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableConfigurationProperties(H2ConsoleProperties.class)
@WithUserDetails(value = "sample@email.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@ActiveProfiles("test")
class ProfileControllerTest extends RestDocsSupport {

    @MockBean
    ProfileImageService profileImageService;

    @Autowired
    UserRepository userRepository;

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
}