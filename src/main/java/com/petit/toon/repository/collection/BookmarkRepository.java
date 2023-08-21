package com.petit.toon.repository.collection;

import com.petit.toon.entity.collection.Bookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("select b from Bookmark b join fetch b.cartoon where b.collection.id = :collectionId")
    List<Bookmark> findBookmarksByCollectionId(long collectionId, Pageable pageable);
}
