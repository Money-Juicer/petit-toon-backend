package com.petit.toon.controller.user;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.service.user.FollowService;
import com.petit.toon.service.user.response.FollowResponse;
import com.petit.toon.service.user.response.FollowUserListResponse;
import com.petit.toon.service.user.response.FollowUserResponse;
import com.petit.toon.service.user.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FollowController.class)
@ActiveProfiles("test")
class FollowControllerTest extends RestDocsSupport {

    @MockBean
    FollowService followService;

    @Test
    @DisplayName("팔로우 등록 API")
    void follow() throws Exception {
        // given
        given(followService.follow(anyLong(), anyLong())).willReturn(new FollowResponse(1l));

        // when // then
        mockMvc.perform(post("/api/v1/follow/{followerId}/{followeeId}", 1, 2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.followId").value(1l))
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("follow-create",
                        pathParameters(
                                parameterWithName("followerId").description("유저 ID"),
                                parameterWithName("followeeId").description("팔로우하려는 유저 ID")
                        ),
                        responseFields(
                                fieldWithPath("followId").description("생성된 팔로우 ID")
                        )
                ));
    }

    @Test
    @DisplayName("팔로우 목록 조회 API")
    void getFollowingUsers() throws Exception {
        // given
        UserResponse userResponse1 = createUser(1l, "김지훈", "@Hotoran");
        UserResponse userResponse2 = createUser(2l, "이용우", "@timel2ss");

        FollowUserResponse followResponse1 = createFollow(1l, userResponse1);
        FollowUserResponse followResponse2 = createFollow(2l, userResponse2);

        FollowUserListResponse response = new FollowUserListResponse(List.of(followResponse1, followResponse2));

        given(followService.findFollowingUsers(anyLong(), any())).willReturn(response);

        // when // then
        mockMvc.perform(get("/api/v1/follow/{userId}?page=0&size=20", 1l))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followUsers").isArray())
                .andExpect(jsonPath("$.followUsers[0].followId").value(1l))
                .andExpect(jsonPath("$.followUsers[1].followId").value(2l))
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("follow-list",
                        pathParameters(
                                parameterWithName("userId").description("유저 ID")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").optional().description("데이터 수")
                        ),
                        responseFields(
                                fieldWithPath("followUsers").type(JsonFieldType.ARRAY)
                                        .description("팔로우 목록 데이터"),
                                fieldWithPath("followUsers[].followId").type(JsonFieldType.NUMBER)
                                        .description("팔로우 ID"),
                                fieldWithPath("followUsers[].user").type(JsonFieldType.OBJECT)
                                        .description("유저 정보"),
                                fieldWithPath("followUsers[].user.id").type(JsonFieldType.NUMBER)
                                        .description("유저 ID"),
                                fieldWithPath("followUsers[].user.name").type(JsonFieldType.STRING)
                                        .description("유저 이름"),
                                fieldWithPath("followUsers[].user.nickname").type(JsonFieldType.STRING)
                                        .description("유저 닉네임")
                        )
                ));
    }

    @Test
    @DisplayName("팔로우 삭제 API")
    void unfollow() throws Exception {
        //given // when // then
        mockMvc.perform(delete("/api/v1/follow/{followId}", 1l))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("follow-delete",
                        pathParameters(
                                parameterWithName("followId").description("팔로우 ID")
                        )));
    }

    private static FollowUserResponse createFollow(long followId, UserResponse userResponse) {
        return FollowUserResponse.builder()
                .followId(followId)
                .user(userResponse)
                .build();
    }

    private UserResponse createUser(long id, String name, String nickname) {
        return UserResponse.builder()
                .id(id)
                .name(name)
                .nickname(nickname)
                .build();
    }
}