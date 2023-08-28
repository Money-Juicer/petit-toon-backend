package com.petit.toon.controller.collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.controller.collection.request.CollectionRequest;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.badrequest.AuthorityNotMatchException;
import com.petit.toon.exception.notfound.CollectionNotFoundException;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.collection.CollectionService;
import com.petit.toon.service.collection.response.*;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@WithUserDetails(value = "sample@email.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@EnableConfigurationProperties(H2ConsoleProperties.class)
class CollectionControllerTest extends RestDocsSupport {

    @MockBean
    CollectionService collectionService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.save(User.builder()
                .email("sample@email.com")
                .password("SamplePW123!@#")
                .build());
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    void createCollection() throws Exception {
        //given
        given(collectionService.createCollection(anyLong(), anyString(), anyBoolean()))
                .willReturn(new CollectionResponse(1l));

        CollectionRequest request = CollectionRequest.builder()
                .title("sample-title")
                .closed(true)
                .build();

        //when // then
        mockMvc.perform(post("/api/v1/collection/create")
                        .content(objectMapper.writeValueAsBytes(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("collection-create",
                        requestFields(
                                fieldWithPath("title").description("Collection 제목"),
                                fieldWithPath("closed").description("비공개 여부 (true: 비공개, false: 공개)")
                        ),
                        responseFields(
                                fieldWithPath("collectionId").type(JsonFieldType.NUMBER)
                                        .description("생성된 Collection ID")
                        )
                ));
    }

    @Test
    void createBookmark() throws Exception {
        // given
        given(collectionService.createBookmark(anyLong(), anyLong(), anyLong()))
                .willReturn(new BookmarkResponse(1l));

        // when // then
        mockMvc.perform(post("/api/v1/collection/{collectionId}/{cartoonId}", 1l, 1l))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("bookmark-create",
                        pathParameters(
                                parameterWithName("collectionId").description("Collection ID"),
                                parameterWithName("cartoonId").description("웹툰 ID")
                        ),
                        responseFields(
                                fieldWithPath("bookmarkId").type(JsonFieldType.NUMBER)
                                        .description("생성된 Bookmark ID")
                        )
                ));
    }

    @Test
    @DisplayName("createBookmark - CollectionNotFound")
    void createBookmark2() throws Exception {
        // given
        given(collectionService.createBookmark(anyLong(), anyLong(), anyLong())).willThrow(new CollectionNotFoundException());

        // when // then
        mockMvc.perform(post("/api/v1/collection/{collectionId}/{cartoonId}", 99999l, 1l))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value(CollectionNotFoundException.MESSAGE))
                .andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("exception-collection-not-found",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("예외 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("createBookmark - AuthorityNotMatch")
    void createBookmark3() throws Exception {
        // given
        given(collectionService.createBookmark(anyLong(), anyLong(), anyLong())).willThrow(new AuthorityNotMatchException());

        // when // then
        mockMvc.perform(post("/api/v1/collection/{collectionId}/{cartoonId}", 1l, 1l))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value(AuthorityNotMatchException.MESSAGE))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("exception-authority-not-match",
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.STRING)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("예외 메시지")
                        )
                ));
    }

    @Test
    void listCollection() throws Exception {
        // given
        CollectionInfoResponse res1 = createCollectionInfo(1l, "title1", false);
        CollectionInfoResponse res2 = createCollectionInfo(2l, "title2", false);
        CollectionInfoResponse res3 = createCollectionInfo(3l, "title3", true);
        given(collectionService.viewCollectionList(anyLong(), anyBoolean(), any()))
                .willReturn(new CollectionInfoListResponse(List.of(res1, res2, res3)));

        // when // then
        mockMvc.perform(get("/api/v1/collection/author/{authorId}", 1l)
                        .param("page", "0")
                        .param("size", "30"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("collection-list",
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").optional().description("데이터 수")
                        ),
                        pathParameters(
                                parameterWithName("authorId").description("조회할 유저 ID")
                        ),
                        responseFields(
                                fieldWithPath("collectionInfos").type(JsonFieldType.ARRAY)
                                        .description("Collection 목록 데이터"),
                                fieldWithPath("collectionInfos[].id").type(JsonFieldType.NUMBER)
                                        .description("Collection ID"),
                                fieldWithPath("collectionInfos[].title").type(JsonFieldType.STRING)
                                        .description("Collection 제목"),
                                fieldWithPath("collectionInfos[].closed").type(JsonFieldType.BOOLEAN)
                                        .description("Collection 비공개 여부 (true: 비공개 | false: 공개)")
                        )
                ));
    }

    @Test
    void listBookmarks() throws Exception {
        // given
        BookmarkInfoResponse res1 = createBookmarkInfo(1l, 2l, "cartoon-title-1", "cartoon/thumbnail-path/1");
        BookmarkInfoResponse res2 = createBookmarkInfo(2l, 4l, "cartoon-title-2", "cartoon/thumbnail-path/2");
        BookmarkInfoResponse res3 = createBookmarkInfo(3l, 7l, "cartoon-title-3", "cartoon/thumbnail-path/3");
        given(collectionService.viewBookmarks(anyLong(), any()))
                .willReturn(new BookmarkInfoListResponse(List.of(res1, res2, res3)));

        // when // then
        mockMvc.perform(get("/api/v1/collection/{collectionId}", 1l)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("bookmark-list",
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").optional().description("데이터 수")
                        ),
                        pathParameters(
                                parameterWithName("collectionId").description("조회할 Collection ID")
                        ),
                        responseFields(
                                fieldWithPath("bookmarkInfos").type(JsonFieldType.ARRAY)
                                        .description("북마크 목록 데이터"),
                                fieldWithPath("bookmarkInfos[].bookmarkId").type(JsonFieldType.NUMBER)
                                        .description("북마크 ID"),
                                fieldWithPath("bookmarkInfos[].cartoonId").type(JsonFieldType.NUMBER)
                                        .description("북마크한 웹툰 ID"),
                                fieldWithPath("bookmarkInfos[].cartoonTitle").type(JsonFieldType.STRING)
                                        .description("북마크한 웹툰 제목"),
                                fieldWithPath("bookmarkInfos[].thumbnailPath").type(JsonFieldType.STRING)
                                        .description("북마크한 웹툰 썸네일 이미지 경로")
                        )
                ));
    }

    @Test
    void deleteCollection() throws Exception {
        //given // when // then
        mockMvc.perform(delete("/api/v1/collection/{collectionId}", 1l))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("collection-delete",
                        pathParameters(
                                parameterWithName("collectionId").description("Collection ID")
                        )));
    }

    @Test
    void deleteBookmark() throws Exception {
        //given // when // then
        mockMvc.perform(delete("/api/v1/bookmark/{bookmarkId}", 1l))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("bookmark-delete",
                        pathParameters(
                                parameterWithName("bookmarkId").description("북마크 ID")
                        )));
    }

    private CollectionInfoResponse createCollectionInfo(long id, String title, boolean closed) {
        return CollectionInfoResponse.builder()
                .id(id)
                .title(title)
                .closed(closed)
                .build();
    }

    private BookmarkInfoResponse createBookmarkInfo(long bookmarkId, long cartoonId, String cartoonTitle, String path) {
        return BookmarkInfoResponse.builder()
                .bookmarkId(bookmarkId)
                .cartoonId(cartoonId)
                .cartoonTitle(cartoonTitle)
                .thumbnailPath(path)
                .build();
    }
}