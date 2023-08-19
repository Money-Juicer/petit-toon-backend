package com.petit.toon.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

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
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return Optional.ofNullable(
                    authorization.substring(BEARER_PREFIX.length(), authorization.length()));
        }
        return Optional.empty();
    }
}
