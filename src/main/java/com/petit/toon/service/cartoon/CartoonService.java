package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.request.CartoonUploadServiceRequest;
import com.petit.toon.service.cartoon.response.CartoonUploadResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartoonService {
    private final CartoonRepository cartoonRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    @Value("${app.toon.dir}")
    private String toonDirectory;

    public CartoonUploadResponse save(CartoonUploadServiceRequest input) throws IOException {
        Long userId = input.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found. id: " + userId));

        Cartoon cartoon = Cartoon.builder()
                .user(user)
                .title(input.getTitle())
                .description(input.getDescription())
                .viewCount(0)
                .build();

        cartoonRepository.save(cartoon);

        List<MultipartFile> toonImages = input.getToonImages();
        List<Image> images = imageService.storeImages(toonImages, cartoon, toonDirectory);
        cartoon.setImages(images);

        String thumbnailPath = imageService.makeThumbnail(new File(images.get(0).getPath()), cartoon, toonDirectory);

        cartoon.setThumbnailPath(thumbnailPath);
        cartoonRepository.save(cartoon);

        return new CartoonUploadResponse(cartoon.getId());
    }

    public void delete(Long toonId) {
        cartoonRepository.deleteById(toonId);
    }

    public void setToonDirectory(String path) {
        this.toonDirectory = path;
    }
}
