package com.petit.toon.repository.user;

import com.petit.toon.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u join fetch u.profileImage where u.id = :userId")
    Optional<User> findUserById(long userId);

    List<User> findAllByIdIn(List<Long> ids);

    @Query("select exists (select u from User u where u.tag = :tag)")
    boolean existsByTag(String tag);
}
