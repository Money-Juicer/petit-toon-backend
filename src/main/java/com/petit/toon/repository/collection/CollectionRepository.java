package com.petit.toon.repository.collection;

import com.petit.toon.entity.collection.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @Query("select c from Collection c where c.user.id = :userId")
    List<Collection> findCollectionsByUserId(long userId);

//    @Query("select c from Collection c join fetch c.bookmarks where c.id = :collectionId")
//    Optional<Collection> findCollectionById(long collectionId);
}