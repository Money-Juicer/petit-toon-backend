package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.ToonRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.dto.input.ToonUploadInput;
import com.petit.toon.service.cartoon.dto.output.ToonUploadOutput;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public ToonUploadOutput save(ToonUploadInput input) throws IOException {
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

        List<Image> images = imageService.storeImages(input.getToonImages(), cartoon, toonDirectory);
        cartoon.setImages(images);

        toonRepository.save(cartoon);

        return new ToonUploadOutput(cartoon.getId());
    }

    public void delete(Long toonId) {
        toonRepository.deleteById(toonId);
    }

    public void setToonDirectory(String path) {
        this.toonDirectory = path;
    }
}
