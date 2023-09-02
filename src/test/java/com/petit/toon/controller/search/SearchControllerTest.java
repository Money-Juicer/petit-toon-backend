package com.petit.toon.controller.search;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.service.cartoon.response.CartoonListResponse;
import com.petit.toon.service.cartoon.response.CartoonResponse;
import com.petit.toon.service.search.SearchService;
import com.petit.toon.service.user.response.UserListResponse;
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
    @DisplayName("유저 검색 API")
    void searchUser() throws Exception {
        // given
        UserResponse user1 = createUser(1L, "김영현 광팬", "@chocoSongEE");
        UserResponse user2 = createUser(2L, "김영현", "@Kinggggg");
        UserResponse user3 = createUser(7L, "패션개발자김영현", "@Fashion");

        UserListResponse searchResponse = new UserListResponse(List.of(user1, user2, user3));
        given(searchService.searchUser(anyString(), any())).willReturn(searchResponse);

        // when // then
        mockMvc.perform(get("/api/v1/search/user")
                        .param("keyword", "김영현")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("search-user",
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
                                fieldWithPath("users[].tag").type(JsonFieldType.STRING)
                                        .description("유저 태그"),
                                fieldWithPath("users[].profileImagePath").type(JsonFieldType.STRING)
                                        .description("유저 프로필 이미지 경로")
                        )
                ));
    }

    @Test
    @DisplayName("웹툰 검색 API")
    void searchCartoon() throws Exception {
        // given
        UserResponse user1 = createUser(1l, "김승환", "@chocoSongEE");

        CartoonResponse toon1 = createToon(1l, "김영현의 모험", user1.getNickname());
        CartoonResponse toon2 = createToon(3l, "용사 김영현", user1.getNickname());

        CartoonListResponse searchResponse = new CartoonListResponse(List.of(toon1, toon2));
        given(searchService.searchCartoon(anyString(), any())).willReturn(searchResponse);

        // when // then
        mockMvc.perform(get("/api/v1/search/toon")
                        .param("keyword", "김영현")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("search-cartoon",
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드"),
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").optional().description("데이터 수")
                        ),
                        responseFields(
                                fieldWithPath("cartoons[]").type(JsonFieldType.ARRAY)
                                        .description("만화 정보 목록 데이터"),
                                fieldWithPath("cartoons[].id").type(JsonFieldType.NUMBER)
                                        .description("만화 ID"),
                                fieldWithPath("cartoons[].title").type(JsonFieldType.STRING)
                                        .description("만화 제목"),
                                fieldWithPath("cartoons[].author").type(JsonFieldType.STRING)
                                        .description("작가 이름"),
                                fieldWithPath("cartoons[].thumbnailUrl").type(JsonFieldType.STRING)
                                        .description("만화 썸네일 url")
                        )
                ));
    }

    @Test
    @DisplayName("검색 API - 입력값 검증 실패 케이스")
    void searchBadRequest() throws Exception {
        // when // then
        mockMvc.perform(get("/api/v1/search/toon")
                        .param("keyword", "김영현?!@#!")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    private CartoonResponse createToon(long id, String title, String author) {
        return CartoonResponse.builder()
                .id(id)
                .title(title)
                .author(author)
                .thumbnailUrl("toons/" + id + "/" + id + "-thumb.png")
                .build();
    }

    private UserResponse createUser(long id, String nickname, String tag) {
        return UserResponse.builder()
                .id(id)
                .nickname(nickname)
                .tag(tag)
                .profileImagePath("profileImages/1.png")
                .build();
    }

}