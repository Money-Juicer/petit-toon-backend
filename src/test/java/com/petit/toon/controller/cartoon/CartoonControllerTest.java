package com.petit.toon.controller.cartoon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.controller.cartoon.request.CartoonUpdateRequest;
import com.petit.toon.controller.cartoon.request.CartoonUploadRequest;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.badrequest.ImageLimitExceededException;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.CartoonService;
import com.petit.toon.service.cartoon.response.CartoonUploadResponse;
import com.petit.toon.service.cartoon.response.ImageInsertResponse;
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
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(H2ConsoleProperties.class)
@WithUserDetails(value = "sample@email.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
public class CartoonControllerTest extends RestDocsSupport {

    @MockBean
    CartoonService cartoonService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    String absolutePath;

    @BeforeEach
    void setUp() {
        String path = "src/test/resources/sample-toons";
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
    @DisplayName("웹툰 등록")
    void upload() throws Exception {
        //given
        given(cartoonService.save(anyLong(), any())).willReturn(new CartoonUploadResponse(1l));

        MockMultipartFile file1 = new MockMultipartFile("toonImages", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));
        MockMultipartFile file2 = new MockMultipartFile("toonImages", "sample2.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample2.png"));

        CartoonUploadRequest request = CartoonUploadRequest.builder()
                .title("sample-title")
                .description("sample-description")
                .toonImages(List.of(file1, file2))
                .build();

        // when // then
        mockMvc.perform(multipart("/api/v1/toon")
                        .file(file1)
                        .file(file2)
                        .part(new MockPart("title", "sample-title".getBytes()),
                                new MockPart("description", "sample-description".getBytes()))
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.toonId").value(1l))
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("toon-create",
                        requestParts(
                                partWithName("title").description("웹툰 제목"),
                                partWithName("description").description("웹툰 설명"),
                                partWithName("toonImages").description("웹툰 이미지 리스트")
                        ),
                        responseFields(
                                fieldWithPath("toonId").type(JsonFieldType.NUMBER)
                                        .description("생성된 웹툰 ID")
                        )
                ));

    }

    @Test
    @DisplayName("웹툰 정보 변경 API")
    void updateToon() throws Exception {
        // given
        CartoonUpdateRequest request = CartoonUpdateRequest.builder()
                .title("update-title")
                .description("update-description")
                .build();

        // when // then
        mockMvc.perform(patch("/api/v1/toon/{toonId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("toon-update",
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("변경할 제목"),
                                fieldWithPath("description").type(JsonFieldType.STRING)
                                        .description("변경할 설명")
                        ),
                        pathParameters(
                                parameterWithName("toonId").description("웹툰 ID")
                        )
                ));
    }

    @Test
    @DisplayName("이미지 삽입 API")
    void insertImage() throws Exception {
        // given
        given(cartoonService.insertImage(anyLong(), anyLong(), anyInt(), any()))
                .willReturn(new ImageInsertResponse("toons/1/1-12.png"));

        MockMultipartFile file = new MockMultipartFile("image", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        // when // then
        mockMvc.perform(multipart("/api/v1/toon/{toonId}/image/{index}", 1L, 3)
                        .file(file))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("toon-insertImage",
//                        pathParameters(
//                                parameterWithName("toonId").description("웹툰 ID"),
//                                parameterWithName("index").description("삽입 위치 +\n (0부터 시작)")
//                        ),
                        responseFields(
                                fieldWithPath("path").type(JsonFieldType.STRING)
                                        .description("새로 삽입된 이미지 경로")
                        )
                ));
    }

    @Test
    @DisplayName("이미지 삽입 API - ImageLimitExceeded")
    void insertImage2() throws Exception {
        // given
        given(cartoonService.insertImage(anyLong(), anyLong(), anyInt(), any()))
                .willThrow(new ImageLimitExceededException());

        MockMultipartFile file = new MockMultipartFile("image", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        // when // then
        mockMvc.perform(multipart("/api/v1/toon/{toonId}/image/{index}", 1L, 3)
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value(ImageLimitExceededException.MESSAGE))
                .andDo(print())
                .andDo(document("exception-image-limit-exceeded",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("예외 메시지")
                        )));
    }

    @Test
    @DisplayName("이미지 삭제 API")
    void removeImage() throws Exception {
        // when // then
        mockMvc.perform(delete("/api/v1/toon/{toonId}/image/{index}", 1L, 3))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("toon-removeImage",
                        pathParameters(
                                parameterWithName("toonId").description("웹툰 ID"),
                                parameterWithName("index").description("삭제 위치 +\n (0부터 시작)")
                        )
                ));
    }

    @Test
    @DisplayName("웹툰 정보 변경 API - null인 필드 허용")
    void updateToon2() throws Exception {
        // given
        CartoonUpdateRequest request = CartoonUpdateRequest.builder()
                .title("update-title")
                .build();

        // when // then
        mockMvc.perform(patch("/api/v1/toon/{toonId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("웹툰 삭제")
    void deleteToon() throws Exception {
        //given // when // then
        mockMvc.perform(delete("/api/v1/toon/{toonId}", 1l))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("toon-delete",
                        pathParameters(
                                parameterWithName("toonId").description("웹툰 ID")
                        )));
    }
}
