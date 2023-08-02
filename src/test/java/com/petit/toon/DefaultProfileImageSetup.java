package com.petit.toon;

import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.repository.user.ProfileImageRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Component
public class DefaultProfileImageSetup {

    @Autowired
    ProfileImageRepository profileImageRepository;

    @PostConstruct
    @Profile("test")
    void setupDefaultProfileImage() throws IOException {
        String defaultImagePath = new File("src/test/resources/sample-profile-images/default.png").getAbsolutePath();
        MultipartFile defaultImageFile = new MockMultipartFile("defaultImage ", "default.png", "multipart/form-data", new FileInputStream(defaultImagePath));

        ProfileImage savedImage = profileImageRepository.save(ProfileImage.builder()
                .fileName(defaultImageFile.getName())
                .originFileName(defaultImageFile.getOriginalFilename())
                .path(defaultImagePath)
                .build());

        System.out.println("savedImage.getId() = " + savedImage.getId());
    }
}
