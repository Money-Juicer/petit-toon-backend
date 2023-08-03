package com.petit.toon.controller.cartoon;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.service.cartoon.LikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LikeController.class)
@ActiveProfiles("test")
class LikeControllerTest extends RestDocsSupport {

    @MockBean
    LikeService likeService;

    @Test
    @DisplayName("좋아요 API")
    void like() throws Exception {
        // given // when // then
        mockMvc.perform(post("/api/v1/like/{userId}/{toonId}", 1, 1))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("like",
                        pathParameters(
                                parameterWithName("userId").description("유저 ID"),
                                parameterWithName("toonId").description("웹툰 ID")
                        )
                ));
    }

    @Test
    @DisplayName("싫어요 API")
    void dislike() throws Exception {
        // given // when // then
        mockMvc.perform(post("/api/v1/dislike/{userId}/{toonId}", 1, 1))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("dislike",
                        pathParameters(
                                parameterWithName("userId").description("유저 ID"),
                                parameterWithName("toonId").description("웹툰 ID")
                        )
                ));
    }
}