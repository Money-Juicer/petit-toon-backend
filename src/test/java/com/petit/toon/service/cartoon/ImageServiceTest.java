package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.ImageRepository;
import com.petit.toon.repository.cartoon.ToonRepository;
import com.petit.toon.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
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

import static java.nio.file.Files.createDirectory;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ImageServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ToonRepository toonRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ImageService imageService;

    @TempDir
    static Path tempDir;
    String absolutePath;
    @BeforeEach
    void setUp() {
        String path = "src/test/resources/sample-toons";
        absolutePath = new File(path).getAbsolutePath();
        System.out.println("tempDirPath = " + tempDir);
    }

    @AfterEach
    void deleteFile() {

    }

    @Test
    @Transactional
    void 이미지저장() throws IOException {
        //given
        User user = createUser("KIM");
        Cartoon mockCartoon = Cartoon.builder()
                .user(user)
                .title("title")
                .description("sample")
                .viewCount(0)
                .build();
        toonRepository.save(mockCartoon);
        createToonDirectory(mockCartoon.getId());

        MultipartFile file = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        //when
        Image img1 = imageService.storeImage(file, mockCartoon, 0, String.valueOf(tempDir));
        Image img2 = imageService.storeImage(file, mockCartoon, 1, String.valueOf(tempDir));

        //then
        assertThat(img1.getId()).isEqualTo(1l);
        assertThat(img1.getFileName()).isEqualTo("1-0.png");
        assertThat(img1.getPath()).isEqualTo(tempDir.resolve(String.valueOf(mockCartoon.getId())).resolve("1-0.png").toString());
        assertThat(img1.getOriginalFileName()).isEqualTo("sample1.png");
        assertThat(img1.getCartoon()).isEqualTo(mockCartoon);

        assertThat(img2.getId()).isEqualTo(2l);
        assertThat(img2.getFileName()).isEqualTo("1-1.png");
        assertThat(img2.getPath()).isEqualTo(tempDir.resolve(String.valueOf(mockCartoon.getId())).resolve("1-1.png").toString());
        assertThat(img2.getOriginalFileName()).isEqualTo("sample1.png");
        assertThat(img2.getCartoon()).isEqualTo(mockCartoon);
    }

    private User createUser(String name) {
        return userRepository.save(
                User.builder()
                        .name(name)
                        .build());
    }

    private void createToonDirectory(Long toonId) throws IOException {
        String absolutePath = tempDir + "/" + toonId;
        String toonDirectoryPath = new File(absolutePath).getAbsolutePath();
        createDirectory(Path.of(toonDirectoryPath));
    }
}
