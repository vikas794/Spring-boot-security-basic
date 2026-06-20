package com.example.security.auth.jwt;

import com.example.security.config.SecurityProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenRevocationService {

    // In production, use Redis or a DB with TTL to store blacklisted tokens
    // Using Caffeine cache with size limit and time-based expiration to bound memory usage
    private final Cache<String, Boolean> blacklistedTokens;

    public TokenRevocationService(SecurityProperties securityProperties) {
        this.blacklistedTokens = Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterWrite(securityProperties.getJwt().getExpirationMs(), TimeUnit.MILLISECONDS)
                .build();
    }

    public void revokeToken(String token) {
        blacklistedTokens.put(token, Boolean.TRUE);
    }

    public boolean isRevoked(String token) {
        return blacklistedTokens.getIfPresent(token) != null;
    }
}
