package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.ToonRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.request.ToonUploadServiceRequest;
import com.petit.toon.service.cartoon.response.ToonUploadResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ToonService {
    private final ToonRepository toonRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    @Value("${app.toon.dir}")
    private String toonDirectory;

    public ToonUploadResponse save(ToonUploadServiceRequest input) throws IOException {
        Long userId = input.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found. id: " + userId));

        Cartoon cartoon = Cartoon.builder()
                .user(user)
                .title(input.getTitle())
                .description(input.getDescription())
                .viewCount(0)
                .build();

        toonRepository.save(cartoon);

        List<MultipartFile> toonImages = input.getToonImages();
        List<Image> images = imageService.storeImages(toonImages, cartoon, toonDirectory);
        cartoon.setImages(images);

        String thumbnailPath = imageService.makeThumbnail(toonImages.get(0), cartoon, toonDirectory);
        cartoon.setThumbnailPath(thumbnailPath);
        toonRepository.save(cartoon);

        return new ToonUploadResponse(cartoon.getId());
    }

    public void delete(Long toonId) {
        toonRepository.deleteById(toonId);
    }

    public void setToonDirectory(String path) {
        this.toonDirectory = path;
    }
}
