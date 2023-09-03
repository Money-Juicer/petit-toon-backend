package com.petit.toon.repository.collection;

import com.petit.toon.entity.collection.Collection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @Query("select c from Collection c left join fetch c.bookmarks where c.user.id = :userId")
    List<Collection> findCollectionsByUserId(long userId, Pageable pageable);

    @Query("select c from Collection c left join fetch c.bookmarks where c.user.id = :userId and c.closed = false")
    List<Collection> findOpenedCollectionsByUserId(long userId, Pageable pageable);
}