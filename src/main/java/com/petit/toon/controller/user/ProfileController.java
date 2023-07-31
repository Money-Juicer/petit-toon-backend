package com.petit.toon.controller.user;

import com.petit.toon.service.user.ProfileImageService;
import com.petit.toon.service.user.response.ProfileImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileImageService profileImageService;

    @PostMapping("/api/v1/user/{userId}/image/upload")
    public ResponseEntity<ProfileImageResponse> uploadProfileImage(@PathVariable("userId") long userId,
                                                                   @RequestPart MultipartFile profileImage) throws IOException {
        ProfileImageResponse response = profileImageService.upload(userId, profileImage);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/api/v1/user/{userId}/image/default")
    public ResponseEntity<Void> updateToDefault(@PathVariable("userId") long userId) {
        profileImageService.updateToDefault(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
