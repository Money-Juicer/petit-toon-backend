package com.petit.toon.service.user;

import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.request.SignupServiceRequest;
import com.petit.toon.service.user.response.SignupResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입")
    void signup() {
        // given
        SignupServiceRequest request = SignupServiceRequest.builder()
                .name("김지훈")
                .nickname("hotoran")
                .email("sample@email.com")
                .password("samplePassword")
                .build();

        // when
        SignupResponse response = userService.register(request);

        // then
        User findUser = userRepository.findOneWithAuthoritiesByEmail(request.getEmail()).get();
        assertThat(findUser.getId()).isEqualTo(response.getUserId());
        assertThat(findUser.getName()).isEqualTo(request.getName());
        assertThat(findUser.getEmail()).isEqualTo(request.getEmail());
        assertThat(passwordEncoder.matches(request.getPassword(), findUser.getPassword())).isTrue();
        assertThat(findUser.getAuthorities().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("중복된 email 주소로 가입하면 예외가 발생한다")
    void signup2() {
        // given
        SignupServiceRequest request = SignupServiceRequest.builder()
                .name("김지훈")
                .nickname("hotoran")
                .email("sample@email.com")
                .password("samplePassword")
                .build();
        userService.register(request);

        // when // then
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이미 사용 중인 이메일 주소입니다");
    }
}