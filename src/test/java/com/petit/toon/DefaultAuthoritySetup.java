package com.petit.toon;

import com.petit.toon.entity.user.Authority;
import com.petit.toon.entity.user.AuthorityType;
import com.petit.toon.repository.user.AuthorityRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
public class DefaultAuthoritySetup {

    @Autowired
    AuthorityRepository authorityRepository;

    @PostConstruct
    @Profile("test")
    void setupDefaultAuthority() {
        authorityRepository.save(new Authority(AuthorityType.USER));
        authorityRepository.save(new Authority(AuthorityType.ADMIN));
    }
}
