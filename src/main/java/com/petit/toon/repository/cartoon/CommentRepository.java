package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c join fetch c.user u where c.cartoon.id = :cartoonId order by case when u.id = :userId then 1 else 0 end desc, c.createdDateTime desc")
    List<Comment> findCommentsByUserIdAndCartoonId(long userId, long cartoonId, Pageable pageable);

    @Query("select c from Comment c join fetch c.cartoon join fetch c.user u where u.id = :userId order by c.createdDateTime desc")
    List<Comment> findCommentsByUserId(long userId, Pageable pageable);


}
