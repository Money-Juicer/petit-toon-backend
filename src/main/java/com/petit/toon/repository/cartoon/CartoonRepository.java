package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartoonRepository extends JpaRepository<Cartoon, Long> {
    @Query("select c from Cartoon c join fetch c.user join fetch c.user.profileImage where c.id = :cartoonId")
    Optional<Cartoon> findCartoonById(long cartoonId);
}
