package com.petit.toon.controller.collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.controller.collection.request.CollectionRequest;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.collection.CollectionService;
import com.petit.toon.service.collection.response.CollectionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(H2ConsoleProperties.class)
@WithUserDetails(value = "sample@email.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class CollectionControllerTest extends RestDocsSupport {

    @MockBean
    CollectionService collectionService;

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
                                fieldWithPath("title").description("재생목록 제목"),
                                fieldWithPath("closed").description("비공개 여부 (true: 비공개, false: 공개)")
                        ),
                        responseFields(
                                fieldWithPath("collectionId").description("생성된 Collection ID")
                        )
                ));
    }

    @Test
    void createBookmark() {
    }

    @Test
    void listCollection() {
    }

    @Test
    void listBookmarks() {
    }

    @Test
    void deleteCollection() {
    }

    @Test
    void deleteBookmark() {
    }
}