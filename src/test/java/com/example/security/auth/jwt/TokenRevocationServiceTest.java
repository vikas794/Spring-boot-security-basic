package com.example.security.auth.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenRevocationServiceTest {

    private TokenRevocationService tokenRevocationService;

    @BeforeEach
    void setUp() {
        com.example.security.config.SecurityProperties properties = new com.example.security.config.SecurityProperties();
        com.example.security.config.SecurityProperties.Jwt jwt = new com.example.security.config.SecurityProperties.Jwt();
        jwt.setExpirationMs(3600000L);
        properties.setJwt(jwt);
        tokenRevocationService = new TokenRevocationService(properties);
    }

    @Test
    void isRevoked_WhenTokenNotRevoked_ReturnsFalse() {
        assertFalse(tokenRevocationService.isRevoked("token123"));
    }

    @Test
    void isRevoked_WhenTokenRevoked_ReturnsTrue() {
        tokenRevocationService.revokeToken("token123");
        assertTrue(tokenRevocationService.isRevoked("token123"));
    }

    @Test
    void isRevoked_WhenDifferentTokenRevoked_ReturnsFalse() {
        tokenRevocationService.revokeToken("token123");
        assertFalse(tokenRevocationService.isRevoked("token456"));
    }

    @Test
    void revokeToken_NullToken_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> tokenRevocationService.revokeToken(null));
    }

    @Test
    void isRevoked_NullToken_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> tokenRevocationService.isRevoked(null));
    }

    @Test
    void revokeToken_EmptyString_SuccessfullyRevokes() {
        tokenRevocationService.revokeToken("");
        assertTrue(tokenRevocationService.isRevoked(""));
    }

    @Test
    void revokeToken_DuplicateRevocation_Idempotent() {
        tokenRevocationService.revokeToken("token123");
        tokenRevocationService.revokeToken("token123");
        assertTrue(tokenRevocationService.isRevoked("token123"));
    }
}
