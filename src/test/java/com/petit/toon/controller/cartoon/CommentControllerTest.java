package com.petit.toon.controller.cartoon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.controller.cartoon.request.CommentCreateRequest;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.CommentService;
import com.petit.toon.service.cartoon.response.*;
import com.petit.toon.service.user.response.UserResponse;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(H2ConsoleProperties.class)
@WithUserDetails(value = "sample@email.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class CommentControllerTest extends RestDocsSupport {

    @MockBean
    CommentService commentService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

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
    @DisplayName("댓글 등록")
    void enrollComment() throws Exception {
        // given
        given(commentService.createComment(anyLong(), anyLong(), anyString()))
                .willReturn(new CommentCreateResponse(1l));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .content("sample-content")
                .build();

        // when // then
        mockMvc.perform(post("/api/v1/comment/{toonId}", 1l)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("comment-create",
                        pathParameters(
                                parameterWithName("toonId").description("웹툰 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").description("댓글 내용 (200자 이내)")),
                        responseFields(
                                fieldWithPath("commentId").type(JsonFieldType.NUMBER)
                                        .description("생성된 댓글 ID")
                        )
                ));

    }

    @Test
    @DisplayName("웹툰 댓글 조회")
    void listComments() throws Exception {
        // given
        UserResponse user1 = createUserResponse(1L, "Hotoran", "hoto_ran", "thumnnail/path/1");
        UserResponse user2 = createUserResponse(2L, "Lepetit", "iced3974", "thumnnail/path/2");
        UserResponse user3 = createUserResponse(3L, "Kimasds", "kimyy333", "thumnnail/path/3");
        UserResponse user4 = createUserResponse(4L, "LEEEEEE", "timel2ss", "thumnnail/path/4");


        CommentResponse res1 = createResponse(1l, user1, "sample 1", true);
        CommentResponse res2 = createResponse(5l, user2, "sample 2", false);
        CommentResponse res3 = createResponse(12l, user3, "sample 3", false);
        CommentResponse res4 = createResponse(13l, user4, "sample 4", false);

        given(commentService.viewComments(anyLong(), anyLong(), any()))
                .willReturn(new CommentListResponse(List.of(res1, res2, res3, res4)));

        mockMvc.perform(get("/api/v1/comment/{toonId}", 1l)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("comment-list",
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").optional().description("데이터 수")
                        ),
                        pathParameters(
                                parameterWithName("toonId").description("웹툰 ID")
                        ),
                        responseFields(
                                fieldWithPath("comments").type(JsonFieldType.ARRAY)
                                        .description("댓글 데이터"),
                                fieldWithPath("comments[].commentId").type(JsonFieldType.NUMBER)
                                        .description("댓글 ID"),

                                fieldWithPath("comments[].userInfo").type(JsonFieldType.OBJECT)
                                        .description("댓글 유저 정보"),
                                fieldWithPath("comments[].userInfo.id").type(JsonFieldType.NUMBER)
                                        .description("댓글 유저 ID"),
                                fieldWithPath("comments[].userInfo.nickname").type(JsonFieldType.STRING)
                                        .description("댓글 유저 닉네임"),
                                fieldWithPath("comments[].userInfo.tag").type(JsonFieldType.STRING)
                                        .description("댓글 유저 태그"),
                                fieldWithPath("comments[].userInfo.profileImagePath").type(JsonFieldType.STRING)
                                        .description("댓글 유저 프로필이미지 경로"),

                                fieldWithPath("comments[].content").type(JsonFieldType.STRING)
                                        .description("댓글 내용"),
                                fieldWithPath("comments[].myComment").type(JsonFieldType.BOOLEAN)
                                        .description("자신의 댓글 여부 (true: 조회한 유저의 댓글 | false: 다른 사람)"),
                                fieldWithPath("comments[].createdDateTime").type(JsonFieldType.STRING)
                                        .description("댓글 생성 시각"),
                                fieldWithPath("comments[].modifiedDateTime").type(JsonFieldType.STRING)
                                        .description("댓글 수정 시각")
                        )
                ));
    }

    @Test
    @DisplayName("자신이 단 댓글 조회")
    void listUserComments() throws Exception {
        // given
        MyCommentResponse res1 = createMyResponse(1l, 12L, "김영현의 모험1", "김영현", "thumbnail/path/1");
        MyCommentResponse res2 = createMyResponse(2l, 14L, "이세계 용사 김영현", "SPR", "thumbnail/path/2");
        MyCommentResponse res3 = createMyResponse(3l, 25L, "태어나보니 김영현", "Inkojava", "thumbnail/path/3");
        MyCommentResponse res4 = createMyResponse(4l, 43L, "김영현의 모험2", "김영현", "thumbnail/path/4");

        given(commentService.viewOnlyMyComments(anyLong(), any()))
                .willReturn(new MyCommentListResponse(List.of(res1, res2, res3, res4)));

        mockMvc.perform(get("/api/v1/comment/myComment")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("comment-list-user",
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").optional().description("데이터 수")
                        ),
                        responseFields(
                                fieldWithPath("comments").type(JsonFieldType.ARRAY)
                                        .description("댓글 데이터"),
                                fieldWithPath("comments[].commentId").type(JsonFieldType.NUMBER)
                                        .description("댓글 ID"),

                                fieldWithPath("comments[].cartoonId").type(JsonFieldType.NUMBER)
                                        .description("댓글 단 웹툰 ID"),
                                fieldWithPath("comments[].cartoonTitle").type(JsonFieldType.STRING)
                                        .description("댓글 단 웹툰 제목"),
                                fieldWithPath("comments[].cartoonThumbnailUrl").type(JsonFieldType.STRING)
                                        .description("댓글 단 웹툰 썸네일 URL"),

                                fieldWithPath("comments[].content").type(JsonFieldType.STRING)
                                        .description("댓글 내용"),
                                fieldWithPath("comments[].createdDateTime").type(JsonFieldType.STRING)
                                        .description("댓글 생성 시각"),
                                fieldWithPath("comments[].modifiedDateTime").type(JsonFieldType.STRING)
                                        .description("댓글 수정 시각")
                        )
                ));
    }


    @Test
    @DisplayName("댓글 삭제")
    void deleteComment() throws Exception {
        //given // when // then
        mockMvc.perform(delete("/api/v1/comment/{commentId}", 1l))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("comment-delete",
                        pathParameters(
                                parameterWithName("commentId").description("댓글 ID")
                        )));
    }

    private CommentResponse createResponse(long commentId, UserResponse userResponse, String content, boolean myComment) {
        LocalDateTime sample = LocalDateTime.now();
        return CommentResponse.builder()
                .commentId(commentId)
                .userInfo(userResponse)
                .content(content)
                .myComment(myComment)
                .createdDateTime(sample)
                .modifiedDateTime(sample)
                .build();
    }

    private UserResponse createUserResponse(long userId, String nickname, String tag, String path) {
        return UserResponse.builder()
                .id(userId)
                .nickname(nickname)
                .tag(tag)
                .profileImagePath(path)
                .build();
    }

    private MyCommentResponse createMyResponse(long commentId, long cartoonId, String title, String path, String content) {
        LocalDateTime sample = LocalDateTime.now();
        return MyCommentResponse.builder()
                .commentId(commentId)
                .cartoonId(cartoonId)
                .cartoonTitle(title)
                .cartoonThumbnailUrl(path)
                .content(content)
                .createdDateTime(sample)
                .modifiedDateTime(sample)
                .build();
    }

    private CartoonResponse createCartoonResponse(long cartoonId, String title, String authorNickname, String path) {
        return CartoonResponse.builder()
                .id(cartoonId)
                .title(title)
                .author(authorNickname)
                .thumbnailUrl(path)
                .build();
    }
}