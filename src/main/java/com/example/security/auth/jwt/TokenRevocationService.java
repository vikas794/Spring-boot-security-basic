package com.example.security.auth.jwt;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenRevocationService {

    // In production, use Redis or a DB with TTL to store blacklisted tokens
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void revokeToken(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isRevoked(String token) {
        return blacklistedTokens.contains(token);
    }
}
