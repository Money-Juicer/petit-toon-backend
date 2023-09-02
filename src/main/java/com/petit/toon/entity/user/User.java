package com.petit.toon.entity.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String nickname;

    private String tag;

    private String email;

    private String password;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ProfileImage profileImage;

    private String statusMessage;

    private boolean isInfluencer;

    @ManyToMany
    @JoinTable(
            name = "user_authorities",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id")
    )
    private Set<Authority> authorities = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdDateTime;

    @UpdateTimestamp
    private LocalDateTime modifiedDateTime;

    @Builder
    private User(String name, String nickname, String tag, String email, String password, String statusMessage) {
        this.name = name;
        this.nickname = nickname;
        this.tag = tag;
        this.email = email;
        this.password = password;
        this.statusMessage = statusMessage;
    }

    public void setProfileImage(ProfileImage profileImage) {
        this.profileImage = profileImage;
    }

    public void assignAuthority(Authority authority) {
        authorities.add(authority);
    }

    public void updateInfluenceStatus(boolean status) {
        this.isInfluencer = status;
    }

    public void updateProfile(User user) {
        if (StringUtils.hasText(user.nickname)) {
            this.nickname = user.nickname;
        }
        if (StringUtils.hasText(user.tag)) {
            this.tag = user.tag;
        }
        if (StringUtils.hasText(user.password)) {
            this.password = user.password;
        }
        if (StringUtils.hasText(user.statusMessage)) {
            this.statusMessage = user.statusMessage;
        }
    }
}
