package com.petit.toon.security;

import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findOneWithAuthoritiesByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found. email: " + username));
        return new CustomUserDetails(user);
    }
}
