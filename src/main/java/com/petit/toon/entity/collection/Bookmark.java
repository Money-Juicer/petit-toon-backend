package com.petit.toon.entity.collection;

import com.petit.toon.entity.cartoon.Cartoon;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    private Cartoon cartoon;

    @Builder
    private Bookmark(Collection collection, Cartoon cartoon) {
        this.collection = collection;
        this.cartoon = cartoon;
    }
}
