package com.petit.toon.entity.cartoon;

import com.petit.toon.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cartoon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String title;

    private String description;

    private int viewCount;

    private String thumbnailPath;

    @OneToMany(mappedBy = "cartoon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "cartoon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdDateTime;

    @UpdateTimestamp
    private LocalDateTime modifiedDateTime;

    @Builder
    private Cartoon(User user, String title, String description, int viewCount, String thumbnailPath) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.viewCount = viewCount;
        this.thumbnailPath = thumbnailPath;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public void increaseViewCount() {
        viewCount++;
    }

    public void updateInfo(Cartoon cartoon) {
        if (StringUtils.hasText(cartoon.title)) {
            this.title = cartoon.title;
        }
        if (StringUtils.hasText(cartoon.description)) {
            this.description = cartoon.description;
        }
    }
}
