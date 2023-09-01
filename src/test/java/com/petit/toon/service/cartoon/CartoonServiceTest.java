package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.LikeStatus;
import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.badrequest.AuthorityNotMatchException;
import com.petit.toon.exception.internalservererror.DefaultProfileImageNotExistException;
import com.petit.toon.exception.notfound.CartoonNotFoundException;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.cartoon.ImageRepository;
import com.petit.toon.repository.user.ProfileImageRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.event.CartoonUploadedEvent;
import com.petit.toon.service.cartoon.request.CartoonUploadServiceRequest;
import com.petit.toon.service.cartoon.response.CartoonDetailResponse;
import com.petit.toon.service.cartoon.response.CartoonListResponse;
import com.petit.toon.service.cartoon.response.CartoonUploadResponse;
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
import java.util.Arrays;

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
}
