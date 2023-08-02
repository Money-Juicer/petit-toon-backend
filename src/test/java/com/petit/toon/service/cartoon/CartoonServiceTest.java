package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.ImageRepository;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.request.CartoonUploadServiceRequest;
import com.petit.toon.service.cartoon.response.CartoonUploadResponse;
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
public class CartoonServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    CartoonService cartoonService;

    @Autowired
    ImageService imageService;

    String absolutePath;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        String path = "src/test/resources/sample-toons";
        absolutePath = new File(path).getAbsolutePath();
        cartoonService.setToonDirectory(String.valueOf(tempDir));
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

        CartoonUploadServiceRequest mockInput1 = CartoonUploadServiceRequest.builder()
                .userId(user.getId())
                .title("sample-title")
                .description("sample-description")
                .toonImages(Arrays.asList(file1, file2, file3))
                .build();

        CartoonUploadServiceRequest mockInput2 = CartoonUploadServiceRequest.builder()
                .userId(user.getId())
                .title("sample-title2")
                .description("sample-description2")
                .toonImages(Arrays.asList(file1, file2))
                .build();
        //when
        CartoonUploadResponse output = cartoonService.save(mockInput1);
        CartoonUploadResponse output2 = cartoonService.save(mockInput2);

        //then
        Cartoon toon = cartoonRepository.findById(output.getToonId()).get();
        assertThat(toon.getId()).isEqualTo(output.getToonId());
        assertThat(toon.getTitle()).isEqualTo("sample-title");
        assertThat(toon.getDescription()).isEqualTo("sample-description");
        assertThat(toon.getImages().get(0).getOriginalFileName()).isEqualTo("sample1.png");
        assertThat(toon.getImages().get(1).getOriginalFileName()).isEqualTo("sample2.png");
        assertThat(toon.getImages().get(2).getOriginalFileName()).isEqualTo("sample3.png");

        Cartoon toon2 = cartoonRepository.findById(output2.getToonId()).get();
        assertThat(toon2.getId()).isEqualTo(output2.getToonId());
        assertThat(toon2.getTitle()).isEqualTo("sample-title2");
        assertThat(toon2.getDescription()).isEqualTo("sample-description2");
        assertThat(toon2.getImages().get(0).getOriginalFileName()).isEqualTo("sample1.png");
        assertThat(toon2.getImages().get(1).getOriginalFileName()).isEqualTo("sample2.png");
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

        cartoonRepository.save(mockCartoon);

        //when
        cartoonService.delete(mockCartoon.getId());

        //then
        assertThat(cartoonRepository.findById(mockCartoon.getId())).isEmpty();
    }

    private User createUser(String name) {
        return userRepository.save(
                User.builder()
                        .name(name)
                        .build());
    }
}
