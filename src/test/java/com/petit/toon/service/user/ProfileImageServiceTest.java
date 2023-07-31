package com.petit.toon.service.user;

import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.ProfileImageRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.response.ProfileImageResponse;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ProfileImageServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileImageRepository profileImageRepository;

    @Autowired
    ProfileImageService profileImageService;

    @TempDir
    static Path tempDir;
    String absolutePath;

    @BeforeEach
    void setup() {
        String path = "src/test/resources/sample-profile-images";
        absolutePath = new File(path).getAbsolutePath();
        profileImageService.setProfileImageDirectory(String.valueOf(tempDir));
    }

    @AfterEach
    void after() {
        userRepository.deleteAllInBatch();
        profileImageRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("프로필 사진 업로드")
    void upload() throws IOException {
        //given
        User user1 = createUser("김영현");
        User user2 = createUser("김승환");
        User user3 = createUser("이용우");

        MultipartFile file1 = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));
        MultipartFile file2 = new MockMultipartFile("sample2.png", "sample2.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample2.png"));

        //when
        ProfileImageResponse response1 = profileImageService.upload(user1.getId(), file1);
        ProfileImageResponse response2 = profileImageService.upload(user2.getId(), file2);

        //then
        ProfileImage profileImage1 = profileImageRepository.findById(response1.getProfileImageId()).get();
        ProfileImage profileImage2 = profileImageRepository.findById(response2.getProfileImageId()).get();

        assertThat(profileImage1.getId()).isEqualTo(response1.getProfileImageId());
        assertThat(profileImage1.getFileName()).isEqualTo(response1.getProfileImageId() + ".png");
        assertThat(profileImage1.getOriginFileName()).isEqualTo("sample1.png");
        assertThat(profileImage1.getPath()).isEqualTo(tempDir.resolve(response1.getProfileImageId() + ".png").toString());

        user1 = userRepository.findById(user1.getId()).get();
        assertThat(user1.getProfileImageId()).isEqualTo(profileImage1.getId());

        assertThat(profileImage2.getId()).isEqualTo(response2.getProfileImageId());
        assertThat(profileImage2.getFileName()).isEqualTo(response2.getProfileImageId() + ".png");
        assertThat(profileImage2.getOriginFileName()).isEqualTo("sample2.png");
        assertThat(profileImage2.getPath()).isEqualTo(tempDir.resolve(response2.getProfileImageId() + ".png").toString());

        user2 = userRepository.findById(user2.getId()).get();
        assertThat(user2.getProfileImageId()).isEqualTo(profileImage2.getId());

        assertThat(user3.getProfileImageId()).isEqualTo(0l);
    }

    @Test
    @DisplayName("Default 이미지로 변경")
    void updateToDefault() throws IOException {
        //given
        User user1 = createUser("김영현");
        MultipartFile file1 = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        ProfileImageResponse response = profileImageService.upload(user1.getId(), file1);
        ProfileImage profileImage = profileImageRepository.findById(response.getProfileImageId()).get();
        user1 = userRepository.findById(user1.getId()).get();

        //when
        profileImageService.updateToDefault(user1.getId());

        //then
        user1 = userRepository.findById(user1.getId()).get();
        assertThat(user1.getProfileImageId()).isEqualTo(0l);
        assertThat(profileImageRepository.findById(profileImage.getId())).isEmpty();

    }

    private User createUser(String name) {
        return userRepository.save(
                User.builder()
                        .name(name)
                        .build());
    }
}