package com.petit.toon.controller.cartoon;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.service.cartoon.CartoonService;
import com.petit.toon.service.cartoon.response.CartoonUploadResponse;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CartoonController.class)
@ActiveProfiles("test")
public class CartoonControllerTest extends RestDocsSupport {

    @MockBean
    CartoonService cartoonService;

    String absolutePath;

    @BeforeEach
    void setUp() {
        String path = "src/test/resources/sample-toons";
        absolutePath = new File(path).getAbsolutePath();
    }

    @Test
    @DisplayName("웹툰 등록")
    void upload() throws Exception {
        //given
        given(cartoonService.save(any())).willReturn(new CartoonUploadResponse(1l));

        MockMultipartFile file1 = new MockMultipartFile("toonImages", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));
        MockMultipartFile file2 = new MockMultipartFile("toonImages", "sample2.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample2.png"));

        // when // then
        mockMvc.perform(multipart("/api/v1/toon")
                        .file(file1)
                        .file(file2)
                        .param("userId", "1")
                        .param("title", "sample-title")
                        .param("description", "sample-description"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.toonId").value(1l))
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("toon-create", responseFields(
                                fieldWithPath("toonId").description("생성된 웹툰 ID")
                        )
                ));
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
