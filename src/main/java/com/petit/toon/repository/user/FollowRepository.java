package com.petit.toon.repository.user;

import com.petit.toon.entity.user.Follow;
import com.petit.toon.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    @Query("select u from User u join Follow f on u.id = f.followee.id where f.follower.id = :followerId")
    List<User> findByFollowerId(long followerId);
}
