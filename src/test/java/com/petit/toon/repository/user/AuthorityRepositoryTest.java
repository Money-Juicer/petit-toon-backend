package com.petit.toon.repository.user;

import com.petit.toon.config.QueryDslConfig;
import com.petit.toon.entity.user.Authority;
import com.petit.toon.entity.user.AuthorityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthorityRepositoryTest {

    @Autowired
    AuthorityRepository authorityRepository;

    @Test
    @DisplayName("사용자 권한을 조회한다")
    void findOneByAuthorityName() {
        // given
        Authority userAuthority = authorityRepository.save(new Authority(AuthorityType.USER));
        Authority adminAuthority = authorityRepository.save(new Authority(AuthorityType.ADMIN));

        // when
        Authority findUserAuthority = authorityRepository.findOneByAuthorityName(AuthorityType.USER).get();
        Authority findAdminAuthority = authorityRepository.findOneByAuthorityName(AuthorityType.ADMIN).get();

        // then
        assertThat(findUserAuthority).isEqualTo(userAuthority);
        assertThat(findAdminAuthority).isEqualTo(adminAuthority);
    }
}