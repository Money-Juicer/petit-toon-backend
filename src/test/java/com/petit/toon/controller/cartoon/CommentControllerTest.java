package com.petit.toon.controller.cartoon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.controller.cartoon.request.CommentCreateRequest;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.CommentService;
import com.petit.toon.service.cartoon.response.*;
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
        CommentResponse res1 = createResponse(1l, 1l, "sample 1", true);
        CommentResponse res2 = createResponse(2l, 5l, "sample 2", false);
        CommentResponse res3 = createResponse(3l, 12l, "sample 3", false);
        CommentResponse res4 = createResponse(4l, 13l, "sample 4", false);

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
                                fieldWithPath("comments[].userId").type(JsonFieldType.NUMBER)
                                        .description("댓글 단 유저 ID"),
                                fieldWithPath("comments[].content").type(JsonFieldType.STRING)
                                        .description("댓글 내용"),
                                fieldWithPath("comments[].myComment").type(JsonFieldType.BOOLEAN)
                                        .description("자신의 댓글 여부 (true: 조회한 유저의 댓글 | false: 다른 사람)")
                        )
                ));
    }

    @Test
    @DisplayName("자신이 단 댓글 조회")
    void listUserComments() throws Exception {
        // given
        MyCommentResponse res1 = createMyResponse(1l, 5l, "sample 1");
        MyCommentResponse res2 = createMyResponse(2l, 11l, "sample 2");
        MyCommentResponse res3 = createMyResponse(3l, 32l, "sample 3");
        MyCommentResponse res4 = createMyResponse(4l, 500l, "sample 4");

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
                                fieldWithPath("comments[].content").type(JsonFieldType.STRING)
                                        .description("댓글 내용")
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

    private CommentResponse createResponse(long userId, long commentId, String content, boolean myComment) {
        return CommentResponse.builder()
                .userId(userId)
                .commentId(commentId)
                .content(content)
                .myComment(myComment)
                .build();
    }

    private MyCommentResponse createMyResponse(long commentId, long cartoonId, String content) {
        return MyCommentResponse.builder()
                .commentId(commentId)
                .cartoonId(cartoonId)
                .content(content)
                .build();
    }

}