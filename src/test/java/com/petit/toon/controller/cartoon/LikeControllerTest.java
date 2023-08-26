package com.petit.toon.controller.cartoon;

import com.petit.toon.controller.RestDocsSupport;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.LikeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(H2ConsoleProperties.class)
@WithUserDetails(value = "sample@email.com", userDetailsServiceBeanName = "customUserDetailsService", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class LikeControllerTest extends RestDocsSupport {

    @MockBean
    LikeService likeService;

    @Autowired
    UserRepository userRepository;

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
    @DisplayName("좋아요 API")
    void like() throws Exception {
        // given // when // then
        mockMvc.perform(post("/api/v1/like/{toonId}", 1))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("like",
                        pathParameters(
                                parameterWithName("toonId").description("웹툰 ID")
                        )
                ));
    }

    @Test
    @DisplayName("싫어요 API")
    void dislike() throws Exception {
        // given // when // then
        mockMvc.perform(post("/api/v1/dislike/{toonId}", 1))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("dislike",
                        pathParameters(
                                parameterWithName("toonId").description("웹툰 ID")
                        )
                ));
    }
}