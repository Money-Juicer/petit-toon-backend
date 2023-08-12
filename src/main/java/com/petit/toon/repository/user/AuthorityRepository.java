package com.petit.toon.repository.user;

import com.petit.toon.entity.user.Authority;
import com.petit.toon.entity.user.AuthorityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Optional<Authority> findOneByAuthorityName(AuthorityType authorityName);
}
