package com.petit.toon.entity.token;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "refresh", timeToLive = 7 * 24 * 60 * 60) // 7 days
public class RefreshToken {

    @Id
    private String id;

    private String ip;

    private Collection<? extends GrantedAuthority> authorities;

    @Indexed
    private String refreshToken;

    @Builder
    private RefreshToken(String id, String ip, Collection<? extends GrantedAuthority> authorities, String refreshToken) {
        this.id = id;
        this.ip = ip;
        this.authorities = authorities;
        this.refreshToken = refreshToken;
    }
}
