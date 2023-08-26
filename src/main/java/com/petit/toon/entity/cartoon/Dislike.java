package com.petit.toon.entity.cartoon;

import com.petit.toon.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "dislikes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "cartoon_id"}))
public class Dislike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Cartoon cartoon;

    @Builder
    private Dislike(User user, Cartoon cartoon) {
        this.user = user;
        this.cartoon = cartoon;
    }
}
