package com.petit.toon.service.user;

import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.ProfileImageRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.response.ProfileImageResponse;
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

    ProfileImage defaultImage;

    @BeforeEach
    void setup() {
        String path = "src/test/resources/sample-profile-images";
        absolutePath = new File(path).getAbsolutePath();
        profileImageService.setProfileImageDirectory(String.valueOf(tempDir));
        defaultImage = profileImageRepository.findById(profileImageService.DEFAULT_PROFILE_IMAGE_ID).get();
    }

    @AfterEach
    void after() {
        userRepository.deleteAllInBatch();
        profileImageRepository.deleteAllInBatch();
    }

    @Test
    @Transactional
    @DisplayName("프로필 사진 업로드")
    void upload() throws IOException {
        //given
        User user1 = createUser("김영현");
        User user2 = createUser("김승환");

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

        assertThat(profileImage1.getFileName()).isEqualTo(response1.getProfileImageId() + ".png");
        assertThat(profileImage1.getOriginFileName()).isEqualTo("sample1.png");
        assertThat(profileImage1.getPath()).isEqualTo(tempDir.resolve(response1.getProfileImageId() + ".png").toString());

        user1 = userRepository.findUserById(user1.getId()).get(); // fetch join
        assertThat(user1.getProfileImage()).isEqualTo(profileImage1);

        assertThat(profileImage2.getFileName()).isEqualTo(response2.getProfileImageId() + ".png");
        assertThat(profileImage2.getOriginFileName()).isEqualTo("sample2.png");
        assertThat(profileImage2.getPath()).isEqualTo(tempDir.resolve(response2.getProfileImageId() + ".png").toString());

        user2 = userRepository.findUserById(user2.getId()).get();
        assertThat(user2.getProfileImage()).isEqualTo(profileImage2);
    }

    @Test
    @Transactional
    @DisplayName("Default 이미지로 변경")
    void updateToDefault() throws IOException {
        //given
        User user1 = createUser("김영현");
        MultipartFile file1 = new MockMultipartFile("sample1.png", "sample1.png", "multipart/form-data",
                new FileInputStream(absolutePath + "/sample1.png"));

        ProfileImageResponse response = profileImageService.upload(user1.getId(), file1);
        ProfileImage profileImage = profileImageRepository.findById(response.getProfileImageId()).get();
        user1 = userRepository.findUserById(user1.getId()).get();

        //when
        profileImageService.updateToDefault(user1.getId());

        //then
        user1 = userRepository.findUserById(user1.getId()).get();
        assertThat(user1.getProfileImage().getId()).isEqualTo(ProfileImageService.DEFAULT_PROFILE_IMAGE_ID);
        assertThat(profileImageRepository.findById(profileImage.getId())).isEmpty();

    }

    private User createUser(String name) {
        User user = userRepository.save(
                User.builder()
                        .name(name)
                        .build());
        user.setProfileImage(defaultImage);
        return userRepository.save(user);
    }
}