package com.petit.toon.controller.user;

import com.petit.toon.service.user.FollowService;
import com.petit.toon.service.user.response.FollowUserListResponse;
import com.petit.toon.service.user.response.FollowUserResponse;
import com.petit.toon.service.user.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FollowController.class)
class FollowControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FollowService followService;

    @Test
    @DisplayName("")
    void follow() throws Exception {
        // given
        given(followService.follow(anyLong(), anyLong())).willReturn(1l);

        // when // then
        mockMvc.perform(post("/api/v1/follow/1/2"))
                .andExpect(status().isCreated())
                .andExpect(content().string("1"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("")
    void getFollowingUsers() throws Exception {
        // given
        UserResponse userResponse1 = createUser(1l, "김지훈", "@Hotoran");
        UserResponse userResponse2 = createUser(2l, "이용우", "@timel2ss");

        FollowUserResponse followResponse1 = createFollow(1l, userResponse1);
        FollowUserResponse followResponse2 = createFollow(2l, userResponse2);

        FollowUserListResponse response = new FollowUserListResponse(List.of(followResponse1, followResponse2));

        given(followService.findFollowingUsers(anyLong(), any())).willReturn(response);

        // when // then
        mockMvc.perform(get("/api/v1/follow/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followUsers").isArray())
                .andExpect(jsonPath("$.followUsers[0].followId").value(1l))
                .andExpect(jsonPath("$.followUsers[1].followId").value(2l))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("")
    void unfollow() throws Exception {
        //given // when // then
        mockMvc.perform(delete("/api/v1/follow/1"))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
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