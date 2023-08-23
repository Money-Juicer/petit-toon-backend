package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.entity.cartoon.LikeStatus;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.notfound.CartoonNotFoundException;
import com.petit.toon.exception.notfound.UserNotFoundException;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.request.CartoonUploadServiceRequest;
import com.petit.toon.service.cartoon.response.CartoonResponse;
import com.petit.toon.service.cartoon.response.CartoonUploadResponse;
import com.petit.toon.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class CartoonService {
    public static final String VIEW_KEY_PREFIX = "view:toon:";

    private final CartoonRepository cartoonRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final LikeService likeService;
    private final RedisUtil redisUtil;

    @Value("${app.toon.dir}")
    private String toonDirectory;

    public CartoonUploadResponse save(long userId, CartoonUploadServiceRequest input) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

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

        String thumbnailPath = imageService.makeThumbnail(new File(getThumbnailPath(images)), cartoon, toonDirectory);

        cartoon.setThumbnailPath(thumbnailPath);
        cartoonRepository.save(cartoon);

        return new CartoonUploadResponse(cartoon.getId());
    }

    @Transactional(readOnly = true)
    public CartoonResponse findOne(long userId, long toonId) {
        Cartoon cartoon = cartoonRepository.findCartoonById(toonId)
                .orElseThrow(CartoonNotFoundException::new);
        long likeCount = likeService.count(toonId);
        LikeStatus likeStatus = likeService.isLiked(userId, toonId);
        return CartoonResponse.of(cartoon, likeCount, likeStatus);
    }

    public void increaseViewCount(long userId, long toonId) {
        String key = VIEW_KEY_PREFIX + toonId;
        if (redisUtil.hasKey(key) && redisUtil.getBit(key, userId)) {
            return;
        }
        redisUtil.setBit(key, userId, true, 1, TimeUnit.DAYS);
        Cartoon cartoon = cartoonRepository.findCartoonById(toonId)
                .orElseThrow(CartoonNotFoundException::new);
        cartoon.increaseViewCount();
    }

    private String getThumbnailPath(List<Image> images) {
        return toonDirectory.substring(0, toonDirectory.indexOf("toons")) + images.get(0).getPath();
    }

    public void delete(Long toonId) {
        cartoonRepository.deleteById(toonId);
    }

    public void setToonDirectory(String path) {
        this.toonDirectory = path;
    }
}
