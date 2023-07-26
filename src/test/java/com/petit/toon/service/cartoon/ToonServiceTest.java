package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.ImageRepository;
import com.petit.toon.repository.cartoon.ToonRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.dto.input.ToonUploadInput;
import com.petit.toon.service.cartoon.dto.output.ToonUploadOutput;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
public class ToonServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ToonRepository toonRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ToonService toonService;

    @Autowired
    ImageService imageService;

    String absolutePath;

    @BeforeEach
    void setUp() {
        String path = "src/test/resources/sample-toons";
        absolutePath = new File(path).getAbsolutePath();
    }

    @Test
    @Transactional
    void 웹툰등록() throws IOException {
        //given
        User user = createUser("KIM");
        MultipartFile file1 = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));
        MultipartFile file2 = new MockMultipartFile("sample2.png", "sample2.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample2.png"));
        MultipartFile file3 = new MockMultipartFile("sample3.png", "sample3.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample3.png"));

        ToonUploadInput mockInput = ToonUploadInput.builder()
                .userId(1l)
                .title("sample-title")
                .description("sample-description")
                .toonImages(Arrays.asList(file1, file2, file3))
                .build();

        //when
        ToonUploadOutput output = toonService.save(mockInput);

        //then
        Cartoon toon = toonRepository.findById(output.getToonId()).get();
        assertThat(toon.getId()).isEqualTo(1l);
        assertThat(toon.getTitle()).isEqualTo("sample-title");
        assertThat(toon.getDescription()).isEqualTo("sample-description");
        assertThat(toon.getImages().get(0).getId()).isEqualTo(1l);
        assertThat(toon.getImages().get(1).getId()).isEqualTo(2l);
        assertThat(toon.getImages().get(2).getId()).isEqualTo(3l);

        toonService.delete(toon.getId());
    }

    /**
     * 웹툰 삭제 테스트
     */
    @Test
    @Transactional
    void 웹툰삭제() {
        //given
        User user = createUser("KIM");
        Cartoon mockCartoon = Cartoon.builder()
                .user(user)
                .title("title")
                .description("sample")
                .viewCount(0)
                .build();
        toonRepository.save(mockCartoon);

        //when
        toonService.delete(1l);

        //then
        assertThat(toonRepository.findById(mockCartoon.getId())).isEmpty();
    }

    private User createUser(String name) {
        return userRepository.save(
                User.builder()
                        .name(name)
                        .build());
    }
}
