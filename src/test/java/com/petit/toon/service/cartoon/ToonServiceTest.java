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
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

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

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        String path = "src/test/resources/sample-toons";
        absolutePath = new File(path).getAbsolutePath();
        toonService.setToonDirectory(String.valueOf(tempDir));
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

        ToonUploadInput mockInput1 = ToonUploadInput.builder()
                .userId(1l)
                .title("sample-title")
                .description("sample-description")
                .toonImages(Arrays.asList(file1, file2, file3))
                .build();

        ToonUploadInput mockInput2 = ToonUploadInput.builder()
                .userId(1l)
                .title("sample-title2")
                .description("sample-description2")
                .toonImages(Arrays.asList(file1, file2))
                .build();
        //when
        ToonUploadOutput output = toonService.save(mockInput1);
        ToonUploadOutput output2 = toonService.save(mockInput2);

        //then
        Cartoon toon = toonRepository.findById(output.getToonId()).get();
        assertThat(toon.getId()).isEqualTo(1l);
        assertThat(toon.getTitle()).isEqualTo("sample-title");
        assertThat(toon.getDescription()).isEqualTo("sample-description");
        assertThat(toon.getImages().get(0).getId()).isEqualTo(1l);
        assertThat(toon.getImages().get(1).getId()).isEqualTo(2l);
        assertThat(toon.getImages().get(2).getId()).isEqualTo(3l);

        Cartoon toon2 = toonRepository.findById(output2.getToonId()).get();
        assertThat(toon2.getId()).isEqualTo(2l);
        assertThat(toon2.getTitle()).isEqualTo("sample-title2");
        assertThat(toon2.getDescription()).isEqualTo("sample-description2");
        assertThat(toon2.getImages().get(0).getId()).isEqualTo(4l);
        assertThat(toon2.getImages().get(1).getId()).isEqualTo(5l);
    }

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
        toonService.delete(mockCartoon.getId());

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
