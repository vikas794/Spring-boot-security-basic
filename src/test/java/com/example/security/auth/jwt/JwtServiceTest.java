package com.example.security.auth.jwt;

import com.example.security.config.SecurityProperties;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private JwtService jwtService;
    private SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        securityProperties = Mockito.mock(SecurityProperties.class);
        SecurityProperties.Jwt jwtProps = new SecurityProperties.Jwt();
        // Generate a 256-bit (32 bytes) secret for HS256
        byte[] secretBytes = new byte[32];
        for(int i = 0; i < secretBytes.length; i++) {
            secretBytes[i] = (byte) i;
        }
        jwtProps.setSecret(Base64.getEncoder().encodeToString(secretBytes));
        jwtProps.setExpirationMs(1000 * 60 * 60); // 1 hour

        when(securityProperties.getJwt()).thenReturn(jwtProps);

        jwtService = new JwtService(securityProperties);
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        UserDetails userDetails = User.withUsername("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testGenerateTokenWithExtraClaims() {
        UserDetails userDetails = User.withUsername("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        Map<String, Object> extraClaims = Collections.singletonMap("customClaim", "customValue");
        String token = jwtService.generateToken(extraClaims, userDetails);

        assertNotNull(token);
        String customClaim = jwtService.extractClaim(token, claims -> claims.get("customClaim", String.class));
        assertEquals("customValue", customClaim);
    }

    @Test
    void testIsTokenValid() {
        UserDetails userDetails = User.withUsername("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));

        UserDetails differentUser = User.withUsername("otheruser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        assertFalse(jwtService.isTokenValid(token, differentUser));
    }

    @Test
    void testIsTokenExpired() throws InterruptedException {
        // Set short expiration time to test expiration
        SecurityProperties.Jwt jwtProps = new SecurityProperties.Jwt();
        byte[] secretBytes = new byte[32];
        for(int i = 0; i < secretBytes.length; i++) { secretBytes[i] = (byte) i; }
        jwtProps.setSecret(Base64.getEncoder().encodeToString(secretBytes));
        jwtProps.setExpirationMs(1); // 1 millisecond
        when(securityProperties.getJwt()).thenReturn(jwtProps);

        UserDetails userDetails = User.withUsername("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        // Wait for token to expire
        Thread.sleep(10);

        // the JJWT library will throw ExpiredJwtException when parsing an expired token
        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenValid(token, userDetails);
        });
    }
}
