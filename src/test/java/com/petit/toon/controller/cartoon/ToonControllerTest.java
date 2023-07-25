package com.petit.toon.controller.cartoon;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.service.cartoon.ToonUploadService;
import com.petit.toon.service.cartoon.dto.output.ToonUploadOutput;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ToonController.class)
@ActiveProfiles("test")
public class ToonControllerTest extends RestDocsSupport {

    @MockBean
    ToonUploadService toonUploadService;

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
        given(toonUploadService.save(any())).willReturn(new ToonUploadOutput(1l));

        MockMultipartFile file1 = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));
        MockMultipartFile file2 = new MockMultipartFile("sample2.png", "sample2.png", "multipart/form-data",
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
                                fieldWithPath("toonId").description("생성된 팔로우 ID")
                        )
                ));
    }
}
