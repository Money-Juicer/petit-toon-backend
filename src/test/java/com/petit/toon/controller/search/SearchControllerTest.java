package com.petit.toon.controller.search;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.service.cartoon.response.CartoonResponse;
import com.petit.toon.service.search.SearchService;
import com.petit.toon.service.search.response.SearchResponse;
import com.petit.toon.service.user.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SearchController.class)
@ActiveProfiles("test")
class SearchControllerTest extends RestDocsSupport {

    @MockBean
    SearchService searchService;

    @Test
    @DisplayName("검색 API")
    void search() throws Exception {
        // given
        UserResponse user1 = createUser(1l, "김승환", "김영현 광팬");

        CartoonResponse toon1 = createToon(1l, "김영현의 모험", "용사 김영현이 모험을 떠난다", user1.getNickname());
        CartoonResponse toon2 = createToon(3l, "용사 김영현", "김영현의 모험 2탄", user1.getNickname());

        SearchResponse searchResponse = new SearchResponse(List.of(user1), List.of(toon1, toon2));
        given(searchService.search(anyString(), any())).willReturn(searchResponse);

        // when // then
        mockMvc.perform(get("/api/v1/search")
                        .param("keyword", "김영현")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("search",
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드"),
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").optional().description("데이터 수")
                        ),
                        responseFields(
                                fieldWithPath("users[]").type(JsonFieldType.ARRAY)
                                        .description("유저 정보 목록 데이터"),
                                fieldWithPath("users[].id").type(JsonFieldType.NUMBER)
                                        .description("유저 ID"),
                                fieldWithPath("users[].nickname").type(JsonFieldType.STRING)
                                        .description("유저 닉네임"),
                                fieldWithPath("users[].profileImagePath").type(JsonFieldType.STRING)
                                        .description("유저 프로필 이미지 경로"),
                                fieldWithPath("toons[]").type(JsonFieldType.ARRAY)
                                        .description("만화 정보 목록 데이터"),
                                fieldWithPath("toons[].id").type(JsonFieldType.NUMBER)
                                        .description("만화 ID"),
                                fieldWithPath("toons[].title").type(JsonFieldType.STRING)
                                        .description("만화 제목"),
                                fieldWithPath("toons[].description").type(JsonFieldType.STRING)
                                        .description("만화 설명"),
                                fieldWithPath("toons[].author").type(JsonFieldType.STRING)
                                        .description("작가 이름"),
                                fieldWithPath("toons[].profileImageUrl").type(JsonFieldType.STRING)
                                        .description("작가 프로필 이미지 경로"),
                                fieldWithPath("toons[].thumbnailUrl").type(JsonFieldType.STRING)
                                        .description("만화 썸네일 url")
                        )
                ));
    }

    @Test
    @DisplayName("검색 API - 입력값 검증 실패 케이스")
    void searchBadRequest() throws Exception {
        // given
        UserResponse user1 = createUser(1l, "김승환", "김영현 광팬");

        CartoonResponse toon1 = createToon(1l, "김영현의 모험", "용사 김영현이 모험을 떠난다", user1.getNickname());
        CartoonResponse toon2 = createToon(3l, "용사 김영현", "김영현의 모험 2탄", user1.getNickname());

        SearchResponse searchResponse = new SearchResponse(List.of(user1), List.of(toon1, toon2));
        given(searchService.search(anyString(), any())).willReturn(searchResponse);

        // when // then
        mockMvc.perform(get("/api/v1/search")
                        .param("keyword", "김영현?!@#!")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    private CartoonResponse createToon(long id, String title, String description, String author) {
        return CartoonResponse.builder()
                .id(id)
                .title(title)
                .description(description)
                .author(author)
                .profileImageUrl("profile-image-url")
                .thumbnailUrl("localhost:8080/static/" + id + "-0.png")
                .build();
    }

    private UserResponse createUser(long id, String name, String nickname) {
        return UserResponse.builder()
                .id(id)
                .nickname(nickname)
                .profileImagePath("profile-image-path")
                .build();
    }

}