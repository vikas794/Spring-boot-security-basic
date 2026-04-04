package com.example.security.core;

public class AuthResponse {
    private String token;
    private String refreshToken; // added for refresh token demo

    public AuthResponse(String token) {
        this.token = token;
        this.refreshToken = "refresh-token-placeholder";
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
