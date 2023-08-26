package com.petit.toon.controller.user;

import com.petit.toon.service.user.ProfileImageService;
import com.petit.toon.service.user.response.ProfileImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileImageService profileImageService;

    @PostMapping("/api/v1/user/image/upload")
    public ResponseEntity<ProfileImageResponse> uploadProfileImage(@AuthenticationPrincipal(expression = "user.id") long userId,
                                                                   @RequestPart MultipartFile profileImage) throws IOException {
        ProfileImageResponse response = profileImageService.upload(userId, profileImage);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/api/v1/user/image/default")
    public ResponseEntity<Void> updateToDefault(@AuthenticationPrincipal(expression = "user.id") long userId) {
        profileImageService.updateToDefault(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
