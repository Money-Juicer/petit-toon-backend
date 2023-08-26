package com.petit.toon.service.cartoon.response;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.entity.cartoon.LikeStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CartoonDetailResponse {
    private long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private long authorId;
    private String authorNickname;
    private String profileImageUrl;
    private List<String> imagePaths;
    private int viewCount;
    private long likeCount;
    private String likeStatus;

    @Builder
    private CartoonDetailResponse(long id, String title, String description, long authorId, String authorNickname, String profileImageUrl, String thumbnailUrl, List<String> imagePaths, int viewCount, long likeCount, String likeStatus) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.authorId = authorId;
        this.authorNickname = authorNickname;
        this.profileImageUrl = profileImageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.imagePaths = imagePaths;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.likeStatus = likeStatus;
    }

    public static CartoonDetailResponse of(Cartoon cartoon, long likeCount, LikeStatus likeStatus) {
        return CartoonDetailResponse.builder()
                .id(cartoon.getId())
                .title(cartoon.getTitle())
                .description(cartoon.getDescription())
                .authorId(cartoon.getUser().getId())
                .authorNickname(cartoon.getUser().getNickname())
                .profileImageUrl(cartoon.getUser().getProfileImage().getPath())
                .thumbnailUrl(cartoon.getThumbnailPath())
                .imagePaths(toImagePaths(cartoon))
                .viewCount(cartoon.getViewCount())
                .likeCount(likeCount)
                .likeStatus(likeStatus.description)
                .build();
    }

    private static List<String> toImagePaths(Cartoon cartoon) {
        return cartoon.getImages().stream().map(Image::getPath).collect(Collectors.toList());
    }
}
