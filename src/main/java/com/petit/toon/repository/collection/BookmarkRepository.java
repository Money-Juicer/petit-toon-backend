package com.petit.toon.repository.collection;

import com.petit.toon.entity.collection.Bookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("select b from Bookmark b join fetch b.cartoon where b.collection.id = :collectionId")
    List<Bookmark> findBookmarksByCollectionId(long collectionId, Pageable pageable);

    @Query("select b from Bookmark b join fetch b.collection join fetch b.collection.user where b.id = :bookmarkId")
    Optional<Bookmark> findBookmarkById(long bookmarkId);
}
