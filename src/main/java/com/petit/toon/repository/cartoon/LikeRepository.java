package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {
    @Query("select l.user.id from Like l where l.cartoon.id = :cartoonId")
    List<Long> findUserIdByCartoonId(long cartoonId);
}
