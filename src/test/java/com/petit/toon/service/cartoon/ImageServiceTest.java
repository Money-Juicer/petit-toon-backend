package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.ImageRepository;
import com.petit.toon.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    ImageRepository imageRepository;

    @Autowired
    ImageService imageService;

    @Value("${app.toon.dir}")
    private String toonStoreDir;

    String absolutePath;
    @BeforeEach
    void setUp() {
        String path = "src/test/resources/sample-toons";
        absolutePath = new File(path).getAbsolutePath();
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
        createToonDirectory(mockCartoon.getId()+1);

        MultipartFile file = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        //when
        Image img = imageService.storeImage(file, mockCartoon, 0);

        //then
        assertThat(img.getFileName()).isEqualTo("1-0.png");
        assertThat(img.getOriginalFileName()).isEqualTo("sample1.png");
        assertThat(img.getCartoon()).isEqualTo(mockCartoon);
        assertThat(img.getId()).isEqualTo(0l);

        imageService.deleteImages(mockCartoon.getId()+1);
    }

    private User createUser(String name) {
        return userRepository.save(
                User.builder()
                        .name(name)
                        .build());
    }

    private void createToonDirectory(Long toonId) throws IOException {
        String absolutePath = toonStoreDir + "/" + toonId;
        String toonDirectoryPath = new File(absolutePath).getAbsolutePath();
        createDirectory(Path.of(toonDirectoryPath));
    }
}
