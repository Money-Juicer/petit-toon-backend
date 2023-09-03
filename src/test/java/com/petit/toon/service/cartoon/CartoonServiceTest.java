package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.entity.cartoon.LikeStatus;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.badrequest.AuthorityNotMatchException;
import com.petit.toon.exception.badrequest.ImageLimitExceededException;
import com.petit.toon.exception.internalservererror.DefaultProfileImageNotExistException;
import com.petit.toon.exception.notfound.CartoonNotFoundException;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.cartoon.ImageRepository;
import com.petit.toon.repository.user.ProfileImageRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.event.CartoonUploadedEvent;
import com.petit.toon.service.cartoon.request.CartoonUpdateServiceRequest;
import com.petit.toon.service.cartoon.request.CartoonUploadServiceRequest;
import com.petit.toon.service.cartoon.response.CartoonDetailResponse;
import com.petit.toon.service.cartoon.response.CartoonListResponse;
import com.petit.toon.service.cartoon.response.CartoonUploadResponse;
import com.petit.toon.service.cartoon.response.ImageInsertResponse;
import com.petit.toon.util.RedisUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.petit.toon.service.user.ProfileImageService.DEFAULT_PROFILE_IMAGE_ID;
import static java.nio.file.Files.createDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RecordApplicationEvents
public class CartoonServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileImageRepository profileImageRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    CartoonService cartoonService;

    @Autowired
    ImageService imageService;

    @Autowired
    ApplicationEvents applicationEvents;

    @Autowired
    RedisUtil redisUtil;

    String absolutePath;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        String path = "src/test/resources/sample-toons";
        absolutePath = new File(path).getAbsolutePath();
        createDirectory(Path.of(tempDir + "/toons"));
        cartoonService.setToonDirectory(tempDir + "/toons");
    }

    @AfterEach
    void tearDown() {
        redisUtil.flushAll();
    }

    @Test
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
                .title("sample-title")
                .description("sample-description")
                .toonImages(Arrays.asList(file1, file2, file3))
                .build();

        CartoonUploadServiceRequest mockInput2 = CartoonUploadServiceRequest.builder()
                .title("sample-title2")
                .description("sample-description2")
                .toonImages(Arrays.asList(file1, file2))
                .build();
        //when
        CartoonUploadResponse output = cartoonService.save(user.getId(), mockInput1);
        CartoonUploadResponse output2 = cartoonService.save(user.getId(), mockInput2);

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

        // then - event evoke test
        assertThat(applicationEvents.stream(CartoonUploadedEvent.class).count()).isEqualTo(2l);
    }

    @Test
    @DisplayName("웹툰 정보를 변경한다")
    void updateCartoonInfo() {
        // given
        User user = createUser("지훈");
        Cartoon cartoon = createCartoon(user);

        CartoonUpdateServiceRequest request = CartoonUpdateServiceRequest.builder()
                .userId(user.getId())
                .toonId(cartoon.getId())
                .title("changed-title")
                .description("changed-description")
                .build();

        // when
        cartoonService.updateCartoonInfo(request);

        // then
        Cartoon findCartoon = cartoonRepository.findById(cartoon.getId()).get();
        assertThat(findCartoon.getTitle()).isEqualTo("changed-title");
        assertThat(findCartoon.getDescription()).isEqualTo("changed-description");
    }

    @Test
    @DisplayName("웹툰 정보를 변경한다 - 웹툰이 존재하지 않는 경우")
    void updateCartoonInfo2() {
        // given
        CartoonUpdateServiceRequest request = CartoonUpdateServiceRequest.builder()
                .userId(1L)
                .toonId(99999L)
                .title("changed-title")
                .description("changed-description")
                .build();

        // when // then
        assertThatThrownBy(() -> cartoonService.updateCartoonInfo(request))
                .isInstanceOf(CartoonNotFoundException.class)
                .hasMessage(CartoonNotFoundException.MESSAGE);
    }

    @Test
    @DisplayName("웹툰 정보를 변경한다 - 변경 권한이 없는 경우")
    void updateCartoonInfo3() {
        // given
        User user = createUser("지훈");
        Cartoon cartoon = createCartoon(user);

        CartoonUpdateServiceRequest request = CartoonUpdateServiceRequest.builder()
                .userId(99999L)
                .toonId(cartoon.getId())
                .title("changed-title")
                .description("changed-description")
                .build();

        // when // then
        assertThatThrownBy(() -> cartoonService.updateCartoonInfo(request))
                .isInstanceOf(AuthorityNotMatchException.class)
                .hasMessage(AuthorityNotMatchException.MESSAGE);
    }

    @Test
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
        cartoonService.delete(user.getId(), mockCartoon.getId());

        //then
        assertThat(cartoonRepository.findById(mockCartoon.getId())).isEmpty();
    }

    @Test
    @DisplayName("웹툰 삭제 - 삭제 권한이 없는 경우")
    void 웹툰삭제2() {
        //given
        User user = createUser("KIM");
        User user2 = createUser("LEE");
        Cartoon mockCartoon = Cartoon.builder()
                .user(user)
                .title("title")
                .description("sample")
                .viewCount(0)
                .build();

        cartoonRepository.save(mockCartoon);

        //when //then
        assertThatThrownBy(() -> cartoonService.delete(user2.getId(), mockCartoon.getId()))
                .isInstanceOf(AuthorityNotMatchException.class)
                .hasMessage(AuthorityNotMatchException.MESSAGE);
    }

    @Test
    @DisplayName("웹툰 삭제 - 웹툰이 존재하지 않는 경우")
    void 웹툰삭제3() {
        //when //then
        assertThatThrownBy(() -> cartoonService.delete(1L, 99999L))
                .isInstanceOf(CartoonNotFoundException.class)
                .hasMessage(CartoonNotFoundException.MESSAGE);
    }

    @Test
    @DisplayName("웹툰 단건 조회")
    void findOne() {
        // given
        User loginUser = createUser("지훈");
        User author = createUser("민서");
        Cartoon cartoon = createCartoon(author);

        // when
        CartoonDetailResponse response = cartoonService.findOne(loginUser.getId(), cartoon.getId());

        // then
        assertThat(response.getId()).isEqualTo(cartoon.getId());
        assertThat(response.getTitle()).isEqualTo(cartoon.getTitle());
        assertThat(response.getAuthorId()).isEqualTo(author.getId());
        assertThat(response.getAuthorNickname()).isEqualTo(author.getNickname());
        assertThat(response.getViewCount()).isEqualTo(0);
        assertThat(response.getLikeCount()).isEqualTo(0);
        assertThat(response.getLikeStatus()).isEqualTo(LikeStatus.NONE.description);
    }

    @Test
    @DisplayName("유저가 게시한 웹툰 목록 조회")
    void findToons() {
        // given
        User user = createUser("민서");
        Cartoon cartoon1 = createCartoon(user);
        Cartoon cartoon2 = createCartoon(user);
        Cartoon cartoon3 = createCartoon(user);

        PageRequest pageRequest = PageRequest.of(0, 5);

        // when
        CartoonListResponse response = cartoonService.findToons(user.getId(), pageRequest);

        // then
        assertThat(response.getCartoons()).extracting("id")
                .contains(cartoon1.getId(), cartoon2.getId(), cartoon3.getId());
    }

    @Test
    @DisplayName("웹툰의 조회수를 1 증가시킨다")
    void increaseViewCount() {
        // given
        User loginUser = createUser("지훈");
        User author = createUser("민서");
        Cartoon cartoon = createCartoon(author);

        // when
        cartoonService.increaseViewCount(loginUser.getId(), cartoon.getId());

        // then
        assertThat(cartoon.getViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("웹툰에 새로운 이미지를 삽입할 수 있다")
    void insertImage1() throws IOException {
        // given
        User user = createUser("hotoran");
        Cartoon cartoon = createCartoon(user);
        createDirectory(Path.of(tempDir + File.separator + "toons" + File.separator + cartoon.getId()));

        Image image1 = createImage(cartoon, getPath(cartoon, 1));
        Image image2 = createImage(cartoon, getPath(cartoon, 2));
        Image image3 = createImage(cartoon, getPath(cartoon, 3));
        Image image4 = createImage(cartoon, getPath(cartoon, 4));
        Image image5 = createImage(cartoon, getPath(cartoon, 5));
        cartoon.setImages(new ArrayList<>(List.of(image1, image2, image3, image4, image5)));

        MultipartFile file = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        // when
        ImageInsertResponse response = cartoonService.insertImage(user.getId(), cartoon.getId(), 3, file);

        // then
        Cartoon findCartoon = cartoonRepository.findById(cartoon.getId()).get();
        assertThat(findCartoon.getImages().size()).isEqualTo(6);
        assertThat(response.getPath()).isEqualTo(getPath(cartoon, 6));
        assertThat(findCartoon.getImages().get(2)).isEqualTo(image3);
        assertThat(findCartoon.getImages().get(4)).isEqualTo(image4);
    }

    @Test
    @DisplayName("웹툰에 새로운 이미지를 삽입할 수 있다 - 웹툰이 존재하지 않는 경우")
    void insertImage2() throws IOException {
        // given
        User user = createUser("hotoran");
        Cartoon cartoon = createCartoon(user);

        MultipartFile file = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        // when // then
        assertThatThrownBy(() -> cartoonService.insertImage(user.getId(), 99999L, 3, file))
                .isInstanceOf(CartoonNotFoundException.class)
                .hasMessage(CartoonNotFoundException.MESSAGE);
    }

    @Test
    @DisplayName("웹툰에 새로운 이미지를 삽입할 수 있다 - 삽입 권한이 없는 경우")
    void insertImage3() throws IOException {
        // given
        User user = createUser("hotoran");
        Cartoon cartoon = createCartoon(user);

        MultipartFile file = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        // when // then
        assertThatThrownBy(() -> cartoonService.insertImage(99999L, cartoon.getId(), 3, file))
                .isInstanceOf(AuthorityNotMatchException.class)
                .hasMessage(AuthorityNotMatchException.MESSAGE);
    }

    @Test
    @DisplayName("웹툰에 새로운 이미지를 삽입할 수 있다 - 이미지 개수를 초과한 경우")
    void insertImage4() throws IOException {
        // given
        User user = createUser("hotoran");
        Cartoon cartoon = createCartoon(user);
        createDirectory(Path.of(tempDir + File.separator + "toons" + File.separator + cartoon.getId()));

        Image image1 = createImage(cartoon, getPath(cartoon, 1));
        Image image2 = createImage(cartoon, getPath(cartoon, 2));
        Image image3 = createImage(cartoon, getPath(cartoon, 3));
        Image image4 = createImage(cartoon, getPath(cartoon, 4));
        Image image5 = createImage(cartoon, getPath(cartoon, 5));
        Image image6 = createImage(cartoon, getPath(cartoon, 6));
        Image image7 = createImage(cartoon, getPath(cartoon, 7));
        Image image8 = createImage(cartoon, getPath(cartoon, 8));
        Image image9 = createImage(cartoon, getPath(cartoon, 9));
        Image image10 = createImage(cartoon, getPath(cartoon, 10));
        cartoon.setImages(new ArrayList<>(List.of(image1, image2, image3, image4, image5,
                image6, image7, image8, image9, image10)));

        MultipartFile file = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        // when // then
        assertThatThrownBy(() -> cartoonService.insertImage(user.getId(), cartoon.getId(), 3, file))
                .isInstanceOf(ImageLimitExceededException.class)
                .hasMessage(ImageLimitExceededException.MESSAGE);
    }

    @Test
    @DisplayName("웹툰에 이미지를 삭제할 수 있다")
    void removeImage() throws IOException {
        // given
        User user = createUser("hotoran");
        Cartoon cartoon = createCartoon(user);
        createDirectory(Path.of(tempDir + File.separator + "toons" + File.separator + cartoon.getId()));

        Image image1 = createImage(cartoon, getPath(cartoon, 1));
        Image image2 = createImage(cartoon, getPath(cartoon, 2));
        Image image3 = createImage(cartoon, getPath(cartoon, 3));
        cartoon.setImages(new ArrayList<>(List.of(image1, image2, image3)));

        // when
        cartoonService.removeImage(user.getId(), cartoon.getId(), 2);

        // then
        Cartoon findCartoon = cartoonRepository.findById(cartoon.getId()).get();
        assertThat(findCartoon.getImages().size()).isEqualTo(2);
        assertThat((new File(tempDir + File.separator + getPath(cartoon, 2))).exists()).isFalse();
    }

    @Test
    @DisplayName("웹툰을 삭제한 자리에 이미지를 삽입하면, 이미지가 덮어쓰기가 된다")
    void replaceImage() throws IOException {
        // given
        User user = createUser("hotoran");
        Cartoon cartoon = createCartoon(user);
        createDirectory(Path.of(tempDir + File.separator + "toons" + File.separator + cartoon.getId()));

        MultipartFile file = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        Image image1 = createImage(cartoon, getPath(cartoon, 1));
        Image image2 = createImage(cartoon, getPath(cartoon, 2));
        Image image3 = createImage(cartoon, getPath(cartoon, 3));
        Image image4 = createImage(cartoon, getPath(cartoon, 4));
        Image image5 = createImage(cartoon, getPath(cartoon, 5));

        cartoon.setImages(new ArrayList<>(List.of(image1, image2, image3, image4, image5)));

        cartoonService.insertImage(user.getId(), cartoon.getId(), 5, file);
        cartoonService.removeImage(user.getId(), cartoon.getId(), 5);


        // when
        MultipartFile file2 = new MockMultipartFile("sample2.png", "sample2.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample2.png"));
        ImageInsertResponse response = cartoonService.insertImage(user.getId(), cartoon.getId(), 5, file2);

        // then
        Cartoon findCartoon = cartoonRepository.findById(cartoon.getId()).get();
        assertThat(findCartoon.getImages().size()).isEqualTo(6);
        assertThat(response.getPath()).isEqualTo(getPath(cartoon, 6));
    }


    private User createUser(String name) {
        User user = User.builder()
                .name(name)
                .nickname("sample-nickname")
                .build();
        ProfileImage profileImage = profileImageRepository.findById(DEFAULT_PROFILE_IMAGE_ID)
                .orElseThrow(DefaultProfileImageNotExistException::new);
        user.setProfileImage(profileImage);
        userRepository.save(user);
        return user;
    }

    private Cartoon createCartoon(User user) {
        return cartoonRepository.save(Cartoon.builder()
                .title("sample-title")
                .user(user)
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
}
