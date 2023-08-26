package com.petit.toon.entity.cartoon;

import com.petit.toon.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "cartoon_id"}))
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Cartoon cartoon;

    @CreationTimestamp
    private LocalDateTime createdDateTime;

    /**
     * setCreatedDateTime method is only for test
     */
    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    @Builder
    private Like(User user, Cartoon cartoon, LocalDateTime createdDateTime) {
        this.user = user;
        this.cartoon = cartoon;
        this.createdDateTime = createdDateTime;
    }
}
