package com.petit.toon.service.user;

import com.petit.toon.entity.user.ProfileImage;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.ProfileImageRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.response.ProfileImageResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfileImageService {

    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;

    @Value("${app.user.image.dir}")
    private String profileImageDirectory;

    /**
     * User Profile Image upload
     */
    public ProfileImageResponse upload(long userId, MultipartFile input) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found. id: " + userId));

        if(user.getProfileImageId() != 0l) {
            profileImageRepository.deleteById(user.getProfileImageId());
        }

        /**
         * Profile Image ID Confirm.
         */
        ProfileImage profileImage = profileImageRepository.save(ProfileImage.builder()
                .originFileName(input.getOriginalFilename())
                .build());

        /**
         * MultipartFile -> BufferdImage (512 x 512 사이즈로 바꾸기 위함)
         */
        BufferedImage inputImage = ImageIO.read(input.getInputStream());
        int width = 512;
        int height = 512;

        /**
         * "{ID}.{ext}" 파일명 구조 및 파일 저장
         */
        String extension = extractExtension(input.getOriginalFilename());
        String storeFileName = profileImage.getId() + "." + extension;
        String storePath = getFullPath(storeFileName, profileImageDirectory);

        profileImage.setFileName(storeFileName);
        profileImage.setPath(storePath);

        File profileImageFile = new File(storePath);
        BufferedImage resizeImage = Scalr.resize(inputImage, width, height);
        ImageIO.write(resizeImage, extension, profileImageFile);

        profileImageRepository.save(profileImage);

        /**
         * user.profileImageId Update.
         */
        user.setProfileImageId(profileImage.getId());
        userRepository.save(user);

        return new ProfileImageResponse(profileImage.getId());
    }

    /**
     * Default ProfileImage로 변경.
     */
    public void updateToDefault(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found. id: " + userId));

        if(user.getProfileImageId() == 0l) {
            return;
        }
        profileImageRepository.deleteById(user.getProfileImageId());

        user.setProfileImageId(0l);
        userRepository.save(user);
    }

    private String extractExtension(String fileName) {
        int idx = fileName.lastIndexOf(".");
        return fileName.substring(idx + 1);
    }

    private String getFullPath(String filename, String directory) {
        String absolutePath = directory + "/" + filename;
        return new File(absolutePath).getAbsolutePath();
    }

    public void setProfileImageDirectory(String profileImageDirectory) {
        this.profileImageDirectory = profileImageDirectory;
    }
}
