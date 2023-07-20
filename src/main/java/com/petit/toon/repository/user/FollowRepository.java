package com.petit.toon.repository.user;

import com.petit.toon.entity.user.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
}
