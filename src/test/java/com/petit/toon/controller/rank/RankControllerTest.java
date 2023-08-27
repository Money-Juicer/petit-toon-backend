package com.petit.toon.controller.rank;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.service.cartoon.response.CartoonResponse;
import com.petit.toon.service.rank.RankService;
import com.petit.toon.service.rank.response.CartoonRankResponse;
import com.petit.toon.service.rank.response.UserRankResponse;
import com.petit.toon.service.user.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RankController.class)
@ActiveProfiles("test")
public class RankControllerTest extends RestDocsSupport {

    @MockBean
    RankService rankService;

    @Test
    @DisplayName("유저 랭킹 API")
    void userRank() throws Exception {
        // given
        UserResponse user1 = createUser(1L, "hotoran", "@hotoran", "profileImages/1.png");
        UserResponse user2 = createUser(5L, "Iced", "@Iced", "profileImages/1.png");
        UserResponse user3 = createUser(2L, "chocosongEE", "@chocosongee", "profileImages/1.png");
        UserResponse user4 = createUser(6L, "timel2ss", "@timel2ss", "profileImages/1.png");

        UserRankResponse response = new UserRankResponse(List.of(user1, user2, user3, user4));

        given(rankService.userRank(any(), any())).willReturn(response);

        // when // then
        mockMvc.perform(get("/api/v1/rank/user")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("rank-user",
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").optional().description("데이터 수")
                        ),
                        responseFields(
                                fieldWithPath("users").type(JsonFieldType.ARRAY)
                                        .description("유저 랭킹 목록"),
                                fieldWithPath("users[].id").type(JsonFieldType.NUMBER)
                                        .description("유저 id"),
                                fieldWithPath("users[].nickname").type(JsonFieldType.STRING)
                                        .description("유저 닉네임"),
                                fieldWithPath("users[].tag").type(JsonFieldType.STRING)
                                        .description("유저 태그"),
                                fieldWithPath("users[].profileImagePath").type(JsonFieldType.STRING)
                                        .description("유저 프로필 이미지 경로")
                        ))
                );
    }

    @Test
    @DisplayName("웹툰 랭킹 API")
    void cartoonRank() throws Exception {
        // given
        CartoonResponse cartoon1 = createCartoon(1L, "김영현의 모험", "Kinggg", "toons/1/1-thumb.png");
        CartoonResponse cartoon2 = createCartoon(3L, "김영현의 모험2", "Kinggg", "toons/3/3-thumb.png");
        CartoonResponse cartoon3 = createCartoon(7L, "김영현의 모험3", "Kinggg", "toons/7/7-thumb.png");
        CartoonResponse cartoon4 = createCartoon(2L, "김영현의 모험4", "Kinggg", "toons/2/2-thumb.png");

        CartoonRankResponse response = new CartoonRankResponse(List.of(cartoon1, cartoon2, cartoon3, cartoon4));

        given(rankService.cartoonRank(any(), any())).willReturn(response);

        // when // then
        mockMvc.perform(get("/api/v1/rank/toon")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("rank-cartoon",
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").optional().description("데이터 수")
                        ),
                        responseFields(
                                fieldWithPath("cartoons").type(JsonFieldType.ARRAY)
                                        .description("만화 랭킹 목록"),
                                fieldWithPath("cartoons[].id").type(JsonFieldType.NUMBER)
                                        .description("만화 id"),
                                fieldWithPath("cartoons[].title").type(JsonFieldType.STRING)
                                        .description("만화 제목"),
                                fieldWithPath("cartoons[].author").type(JsonFieldType.STRING)
                                        .description("작가 이름"),
                                fieldWithPath("cartoons[].thumbnailUrl").type(JsonFieldType.STRING)
                                        .description("만화 썸네일 url")
                        ))
                );
    }

    private UserResponse createUser(long id, String nickname, String tag, String profileImagePath) {
        return UserResponse.builder()
                .id(id)
                .nickname(nickname)
                .tag(tag)
                .profileImagePath(profileImagePath)
                .build();
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