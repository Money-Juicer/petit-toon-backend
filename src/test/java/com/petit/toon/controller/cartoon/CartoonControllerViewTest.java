package com.petit.toon.controller.cartoon;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.notfound.CartoonNotFoundException;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.CartoonService;
import com.petit.toon.service.cartoon.response.CartoonDetailResponse;
import com.petit.toon.service.cartoon.response.CartoonListResponse;
import com.petit.toon.service.cartoon.response.CartoonResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(H2ConsoleProperties.class)
@WithUserDetails(value = "sample@email.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
public class CartoonControllerViewTest extends RestDocsSupport {
    @MockBean
    CartoonService cartoonService;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setup() {
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
    @DisplayName("웹툰 단건 조회 API")
    void getToon() throws Exception {
        // given
        CartoonDetailResponse response = CartoonDetailResponse.builder()
                .id(1L)
                .title("sample-title")
                .description("sample-description")
                .authorId(3L)
                .authorNickname("sample-author")
                .profileImageUrl("profileImages/1.png")
                .viewCount(100)
                .imagePaths(List.of("toons/1/1-0.png", "toons/1/1-1.png"))
                .thumbnailUrl("toons/1/1-thumb.png")
                .likeCount(42)
                .likeStatus("LIKE")
                .build();
        given(cartoonService.findOne(anyLong(), anyLong())).willReturn(response);

        // when // then
        mockMvc.perform(get("/api/v1/toon/{toonId}", 1))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("toon-get",
                        pathParameters(
                                parameterWithName("toonId").description("웹툰 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("만화 ID"),
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("만화 제목"),
                                fieldWithPath("description").type(JsonFieldType.STRING)
                                        .description("만화 설명"),
                                fieldWithPath("authorId").type(JsonFieldType.NUMBER)
                                        .description("작가 유저 ID"),
                                fieldWithPath("authorNickname").type(JsonFieldType.STRING)
                                        .description("작가 이름"),
                                fieldWithPath("profileImageUrl").type(JsonFieldType.STRING)
                                        .description("작가 프로필 이미지 경로"),
                                fieldWithPath("thumbnailUrl").type(JsonFieldType.STRING)
                                        .description("만화 썸네일 url"),
                                fieldWithPath("imagePaths").type(JsonFieldType.ARRAY)
                                        .description("만화 이미지 경로"),
                                fieldWithPath("viewCount").type(JsonFieldType.NUMBER)
                                        .description("만화 조회수"),
                                fieldWithPath("likeCount").type(JsonFieldType.NUMBER)
                                        .description("만화 좋아요수"),
                                fieldWithPath("likeStatus").optional().type(JsonFieldType.STRING)
                                        .description("만화 좋아요 상태 (LIKE/DISLIKE/NONE)")
                        )
                ));
    }

    @Test
    @DisplayName("웹툰 단건 조회 API - CartoonNotFound")
    void getToon2() throws Exception {
        // given
        given(cartoonService.findOne(anyLong(), anyLong())).willThrow(new CartoonNotFoundException());

        // when // then
        mockMvc.perform(get("/api/v1/toon/{toonId}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value(CartoonNotFoundException.MESSAGE))
                .andDo(print())
                .andDo(document("exception-cartoon-not-found",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("예외 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("유저 페이지의 웹툰 목록 조회 API")
    void getToons() throws Exception {
        // given
        CartoonResponse cartoon1 = createCartoon(1L, "김영현의 모험1", "king", "toons/1/1-thumb.png");
        CartoonResponse cartoon2 = createCartoon(6L, "김영현의 모험2", "king", "toons/6/6-thumb.png");
        CartoonResponse cartoon3 = createCartoon(12L, "김영현의 모험3", "king", "toons/12/12-thumb.png");

        CartoonListResponse response = new CartoonListResponse(List.of(cartoon1, cartoon2, cartoon3));

        given(cartoonService.findToons(anyLong(), any())).willReturn(response);

        // when // then
        mockMvc.perform(get("/api/v1/toon/user/{userId}", 1L))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("toon-get-user",
                        pathParameters(
                                parameterWithName("userId").description("유저 ID")
                        ),
                        responseFields(
                                fieldWithPath("cartoons").type(JsonFieldType.ARRAY)
                                        .description("만화 목록"),
                                fieldWithPath("cartoons[].id").type(JsonFieldType.NUMBER)
                                        .description("만화 ID"),
                                fieldWithPath("cartoons[].title").type(JsonFieldType.STRING)
                                        .description("만화 제목"),
                                fieldWithPath("cartoons[].author").type(JsonFieldType.STRING)
                                        .description("작가 닉네임"),
                                fieldWithPath("cartoons[].thumbnailUrl").type(JsonFieldType.STRING)
                                        .description("만화 썸네일 경로")
                        )
                ));
    }

    @Test
    @DisplayName("웹툰 조회수 증가 API")
    void view() throws Exception {
        // when // then
        mockMvc.perform(post("/api/v1/toon/{toonId}/view", 1))
                .andExpect(status().isNoContent())
                .andDo(print())
                .andDo(document("toon-view",
                        pathParameters(
                                parameterWithName("toonId").description("웹툰 ID")
                        )));
    }

    private CartoonResponse createCartoon(long id, String title, String author, String thumbnailUrl) {
        return CartoonResponse.builder()
                .id(id)
                .title(title)
                .author(author)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
