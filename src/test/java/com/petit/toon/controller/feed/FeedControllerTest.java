package com.petit.toon.controller.feed;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.feed.FeedService;
import com.petit.toon.service.feed.response.FeedResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@WithUserDetails(value = "sample@email.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@EnableConfigurationProperties(H2ConsoleProperties.class)
class FeedControllerTest extends RestDocsSupport {

    @Autowired
    UserRepository userRepository;

    @MockBean
    FeedService feedService;

    @BeforeEach
    void setUp() {
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
    void feedInquiry() throws Exception {
        // given
        given(feedService.feed(any(), anyLong(), any())).willReturn(new FeedResponse(List.of(1L, 2L, 3L, 4L)));

        // when then
        mockMvc.perform((get("/api/v1/feed")
                        .param("page", "0")
                        .param("size", "10")))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andDo(document("feed",
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").optional().description("데이터 수 +\n(default size: 10)")
                        ),
                        responseFields(
                                fieldWithPath("feed").type(JsonFieldType.ARRAY)
                                        .description("웹툰 ID 목록 +\n(최대 개수 [요청 데이터 수 * 3])")))
                );

    }
}