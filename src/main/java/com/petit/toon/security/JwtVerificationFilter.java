package com.petit.toon.security;

import com.petit.toon.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {
    private static final String ACCESS_TOKEN_NAME = "accessToken";

    private final JwtTokenProvider jwtTokenProvider;

    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> accessToken = parseTokenFromHeader(request);
        if (accessToken.isPresent() && jwtTokenProvider.validateToken(accessToken.get())) {
            Authentication authentication = jwtTokenProvider.getAuthentication(accessToken.get());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private Optional<String> parseTokenFromHeader(HttpServletRequest request) {
        return cookieUtil.get(request, ACCESS_TOKEN_NAME).map(Cookie::getValue);
    }
}
