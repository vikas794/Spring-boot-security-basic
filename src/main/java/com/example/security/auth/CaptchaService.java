package com.example.security.auth;

import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

    /**
     * Validates the provided CAPTCHA token.
     * WHY: We need to prevent automated attacks such as brute force and credential stuffing.
     * This simulated validation checks if the token is not null and not empty.
     */
    public boolean isValid(String token) {
        return token != null && !token.trim().isEmpty();
    }
}
