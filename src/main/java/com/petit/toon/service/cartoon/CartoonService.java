package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.entity.cartoon.LikeStatus;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.badrequest.AuthorityNotMatchException;
import com.petit.toon.exception.badrequest.ImageLimitExceededException;
import com.petit.toon.exception.notfound.CartoonNotFoundException;
import com.petit.toon.exception.notfound.UserNotFoundException;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.event.CartoonUploadedEvent;
import com.petit.toon.service.cartoon.request.CartoonUpdateServiceRequest;
import com.petit.toon.service.cartoon.request.CartoonUploadServiceRequest;
import com.petit.toon.service.cartoon.response.*;
import com.petit.toon.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CartoonService {
    public static final String VIEW_KEY_PREFIX = "view:toon:";
    public static final int MAX_NUMBER_OF_IMAGES = 10;


    private final ApplicationEventPublisher publisher;

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

        String thumbnailPath = imageService.makeThumbnail(new File(getThumbnailPath(images.get(0))), cartoon, toonDirectory);

        cartoon.setThumbnailPath(thumbnailPath);
        cartoonRepository.save(cartoon);

        if (!user.isInfluencer()) {
            publisher.publishEvent(new CartoonUploadedEvent(userId, cartoon.getId()));
        }

        return new CartoonUploadResponse(cartoon.getId());
    }

    @Transactional(readOnly = true)
    public CartoonDetailResponse findOne(long userId, long toonId) {
        Cartoon cartoon = cartoonRepository.findCartoonById(toonId)
                .orElseThrow(CartoonNotFoundException::new);
        long likeCount = likeService.count(toonId, true);
        LikeStatus likeStatus = likeService.isLiked(userId, toonId);
        return CartoonDetailResponse.of(cartoon, likeCount, likeStatus);
    }

    @Transactional(readOnly = true)
    public CartoonListResponse findToons(long userId, Pageable pageable) {
        List<Cartoon> cartoons = cartoonRepository.findAllByUserId(userId, pageable);
        List<CartoonResponse> cartoonList = cartoons.stream()
                .map(CartoonResponse::of)
                .collect(Collectors.toList());
        return new CartoonListResponse(cartoonList);
    }

    public ImageInsertResponse insertImage(long userId, long toonId, int index, MultipartFile multipartFile) {
        Cartoon cartoon = cartoonRepository.findById(toonId)
                .orElseThrow(CartoonNotFoundException::new);

        if (cartoon.getUser().getId() != userId) {
            throw new AuthorityNotMatchException();
        }

        if (cartoon.getImages().size() >= MAX_NUMBER_OF_IMAGES) {
            throw new ImageLimitExceededException();
        }

        int imageNumber = imageService.getLastImageNumber(cartoon);
        try {
            Image image = imageService.storeImage(multipartFile, cartoon, imageNumber + 1, toonDirectory);
            if (index == 0) {
                imageService.makeThumbnail(new File(getThumbnailPath(image)), cartoon, toonDirectory);
            }
            cartoon.insertImage(image, index);
            return new ImageInsertResponse(image.getPath());
        } catch (Exception e) {
            imageService.deleteFile(imageService.getFullPath(multipartFile, cartoon.getId(), imageNumber + 1, toonDirectory));
            throw new RuntimeException(e);
        }
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

    public void updateCartoonInfo(CartoonUpdateServiceRequest request) {
        Cartoon cartoon = cartoonRepository.findById(request.getToonId())
                .orElseThrow(CartoonNotFoundException::new);

        if (cartoon.getUser().getId() != request.getUserId()) {
            throw new AuthorityNotMatchException();
        }

        Cartoon updateForm = Cartoon.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        cartoon.updateInfo(updateForm);
    }

    public void delete(Long userId, Long toonId) {
        Cartoon cartoon = cartoonRepository.findById(toonId)
                .orElseThrow(CartoonNotFoundException::new);
        if (cartoon.getUser().getId() != userId) {
            throw new AuthorityNotMatchException();
        }
        cartoonRepository.deleteById(toonId);
    }

    public void removeImage(long userId, long toonId, int index) {
        Cartoon cartoon = cartoonRepository.findById(toonId)
                .orElseThrow(CartoonNotFoundException::new);

        if (cartoon.getUser().getId() != userId) {
            throw new AuthorityNotMatchException();
        }

        cartoon.getImages().remove(index);
    }

    public void setToonDirectory(String path) {
        this.toonDirectory = path;
    }

    private String getThumbnailPath(Image image) {
        return toonDirectory.substring(0, toonDirectory.indexOf("toons")) + image.getPath();
    }
}
