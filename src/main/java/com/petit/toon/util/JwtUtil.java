package com.petit.toon.util;

import com.petit.toon.security.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtUtil {
    private static final long ACCESS_TOKEN_VALID_TIME = TimeUnit.MINUTES.toMillis(30);
    private static final long REFRESH_TOKEN_VALID_TIME = TimeUnit.DAYS.toMillis(7);

    private CustomUserDetailsService userDetailsService;

    @Autowired
    public JwtUtil(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;

    @PostConstruct
    public void decodeSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Authentication authentication) {
        return generateToken(authentication, ACCESS_TOKEN_VALID_TIME);
    }

    public String createAccessToken(String email) {
        return generateToken(email, ACCESS_TOKEN_VALID_TIME);
    }

    public String createRefreshToken(Authentication authentication) {
        return generateToken(authentication, REFRESH_TOKEN_VALID_TIME);
    }

    public String createRefreshToken(String email) {
        return generateToken(email, REFRESH_TOKEN_VALID_TIME);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaimsJws(token).getBody();
        UserDetails principle = userDetailsService.loadUserByUsername(claims.getSubject());
        return new UsernamePasswordAuthenticationToken(principle, token, new ArrayList<>());
    }

    private String generateToken(Authentication authentication, long expireTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireTime);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("email", userDetails.getUsername())
                .signWith(key, SignatureAlgorithm.HS256)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .compact();

        log.info("create token: {}, expiration; {}", token, expiration);
        return token;
    }

    private String generateToken(String email, long expireTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireTime);

        String token = Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .signWith(key, SignatureAlgorithm.HS256)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .compact();

        log.info("create refresh token: {}, expiration; {]", token, expiration);
        return token;
    }

    public boolean validateToken(String token) {
        try {
            parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다");
        }
        return false;
    }

    public String getUsername(String token) {
        return parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
    }

    private Jws<Claims> parseClaimsJws(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
