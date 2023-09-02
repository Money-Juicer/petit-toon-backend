package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Dislike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DislikeRepository extends JpaRepository<Dislike, Long>, CustomDislikeRepository {
    @Query("select dl.user.id from Dislike dl where dl.cartoon.id = :cartoonId")
    List<Long> findUserIdByCartoonId(long cartoonId);

    @Modifying
    @Query("delete from Dislike dl where dl.id in :ids")
    void deleteAllByIdIn(List<Long> ids);
}
