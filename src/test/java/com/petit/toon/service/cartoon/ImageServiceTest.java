package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.cartoon.ImageRepository;
import com.petit.toon.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.List;

import static java.nio.file.Files.createDirectory;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ImageServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ImageService imageService;

    @TempDir
    Path tempDir;
    String absolutePath;
    @BeforeEach
    void setUp() throws IOException {
        String path = "src/test/resources/sample-toons";
        absolutePath = new File(path).getAbsolutePath();
        System.out.println("tempDirPath = " + tempDir);
        createDirectory(Path.of(tempDir + "/toons"));
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
        cartoonRepository.save(mockCartoon);
        createToonDirectory(mockCartoon.getId());

        MultipartFile file = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        //when
        Image img1 = imageService.storeImage(file, mockCartoon, 0, tempDir + "/toons");
        Image img2 = imageService.storeImage(file, mockCartoon, 1, tempDir + "/toons");

        //then
        assertThat(img1.getFileName()).isEqualTo(img1.getCartoon().getId() + "-0.png");
        assertThat(img1.getPath()).isEqualTo("toons" + File.separator + mockCartoon.getId() + File.separator + img1.getCartoon().getId() + "-0.png");
        assertThat(img1.getOriginalFileName()).isEqualTo("sample1.png");
        assertThat(img1.getCartoon()).isEqualTo(mockCartoon);

        assertThat(img2.getFileName()).isEqualTo(img1.getCartoon().getId() + "-1.png");
        assertThat(img2.getPath()).isEqualTo("toons" + File.separator + mockCartoon.getId() + File.separator + img1.getCartoon().getId() + "-1.png");
        assertThat(img2.getOriginalFileName()).isEqualTo("sample1.png");
        assertThat(img2.getCartoon()).isEqualTo(mockCartoon);
    }

    @Test
    @DisplayName("이미지 경로로부터 이미지 번호를 파싱한다")
    void extractImageNumber() {
        // given
        String path = "toons/1/1-3.png";

        // when
        int number = Integer.parseInt(path.split("-|\\.")[1]);

        // then
        assertThat(number).isEqualTo(3);
    }

    @Test
    @DisplayName("웹툰의 마지막 번호를 가져온다")
    void getLastImageNumber() {
        // given
        User user = createUser("hotoran");
        Cartoon cartoon = createCartoon(user, "김영현의 모험");
        Image image1 = createImage(cartoon, getPath(cartoon, 1));
        Image image2 = createImage(cartoon, getPath(cartoon, 2));
        Image image3 = createImage(cartoon, getPath(cartoon, 3));
        Image image4 = createImage(cartoon, getPath(cartoon, 4));
        Image image5 = createImage(cartoon, getPath(cartoon, 5));
        cartoon.setImages(List.of(image1, image2, image3, image4, image5));

        // when
        int lastIndex = imageService.getLastImageNumber(cartoon);

        // then
        assertThat(lastIndex).isEqualTo(5);
    }

    private User createUser(String name) {
        return userRepository.save(
                User.builder()
                        .name(name)
                        .build());
    }

    private Cartoon createCartoon(User user, String title) {
        return cartoonRepository.save(
                Cartoon.builder()
                        .user(user)
                        .title(title)
                        .build());
    }

    private Image createImage(Cartoon cartoon, String path) {
        return imageRepository.save(
                Image.builder()
                        .cartoon(cartoon)
                        .path(path)
                        .build());
    }

    private String getPath(Cartoon cartoon, int index) {
        return "toons" + File.separator + cartoon.getId() + File.separator + cartoon.getId() + "-" + index + ".png";
    }

    private void createToonDirectory(Long toonId) throws IOException {
        String absolutePath = tempDir + "/toons/" + toonId;
        String toonDirectoryPath = new File(absolutePath).getAbsolutePath();
        createDirectory(Path.of(toonDirectoryPath));
    }
}
